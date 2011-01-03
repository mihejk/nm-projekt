package hr.fer.nm_projekt.utilities;

import hr.fer.nm_projekt.Category;
import hr.fer.nm_projekt.MainClassifierImpl;
import hr.fer.nm_projekt.preprocessing.ImageBinarizer;
import hr.fer.nm_projekt.preprocessing.ImageLineThinning;
import hr.fer.nm_projekt.preprocessing.ImageNoiseRemover;
import hr.fer.nm_projekt.preprocessing.ImageScaler;
import hr.fer.nm_projekt.preprocessing.ImageStainRemover;
import hr.fer.nm_projekt.preprocessing.ImageTransformer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;

/*
 * postavite ovaj folderName na fodler di su svi oni skenovi
 * kad se pokrene, s lijeve strane se pokazu folderi, a onda kad se double klikne se s desne strane izlistaju sve slikice,
 * kao i rezultati poziva transform()
 * u transform stavljajte svoje neke kodove, trenutno je tamo primjer jednostavne binarizacije
 * moze se kliknut na sliku s desne strane, pa se jos poveca
 * 
 */

public class Tester extends JFrame {

	private static final long serialVersionUID = 1L;
	private static String folderName = "data";
	private static final int dimensions = 12;
	
	private List<ImageTransformer> transformers;

	public static void main(String[] args) {

		List<ImageTransformer> transformers = new LinkedList<ImageTransformer>();

		transformers.add(new ImageBinarizer());
		transformers.add(new ImageNoiseRemover());
		transformers.add(new ImageStainRemover());
		transformers.add(new ImageScaler(50, 50));
		transformers.add(new ImageLineThinning());

		new Tester(transformers);
	}

	private class BarChart extends JPanel {
		private static final long serialVersionUID = 1L;
		private int[] data;
		private String[] labels;
		private String title;
		private double[] percentage;
		
		public BarChart( int[] d, String[] l, String t ) {
			data = d;
			labels = l;
			title = t;
			
			int sum = 0;
			for( int i = 0; i < data.length; ++i ) sum += data[i];
			
			percentage = new double[data.length];
			for( int i = 0; i < data.length; ++i ) percentage[i] = (double) data[i] / sum;
		}

		public void paintComponent(Graphics graphics) {
			super.paintComponent(graphics);
		    Dimension dim = getSize();
		    int width = dim.width;
		    int height = dim.height;
		    int max = 0;
		    
		    for( int i = 0; i < data.length; ++i )
		    	if( data[i] > max ) max = data[i];
		    
		    int barWidth = width / data.length;

		    Font titleFont = new Font("Verdana", Font.BOLD, 15);
		    FontMetrics titleFontMetrics = graphics.getFontMetrics(titleFont);
		    Font labelFont = new Font("Verdana", Font.BOLD, 10);
		    FontMetrics labelFontMetrics = graphics.getFontMetrics(labelFont);
			
		    int titleWidth = titleFontMetrics.stringWidth(title);
		    int q = titleFontMetrics.getAscent();
		    int p = (width - titleWidth) / 2;

		    graphics.setFont(titleFont);
		    graphics.drawString(title, p, q);
			
		    int top = titleFontMetrics.getHeight();
			int bottom = labelFontMetrics.getHeight();

		    double scale = (double) (height - top - bottom*2) / max;
		    int descent = labelFontMetrics.getDescent(); 
		    q = height - bottom - descent;
		    graphics.setFont(labelFont);
		    
		    DecimalFormat df = new DecimalFormat("#.##%");
		    
		    for (int j = 0; j < data.length; ++j ) {
		    	int valueP = j * barWidth + 1;
		    	int valueQ = top;
		    	int h = (int) (data[j] * scale);

		    	valueQ += (int) ((max - data[j]) * scale);

		    	graphics.setColor(Color.blue);
		    	graphics.fillRect(valueP, valueQ, barWidth - 2, h);
		    	graphics.setColor(Color.black);
		    	graphics.drawRect(valueP, valueQ, barWidth - 2, h);
		    	if( labels != null ) {
		    		int labelWidth = labelFontMetrics.stringWidth(labels[j]);
			    	p = j * barWidth + (barWidth - labelWidth) / 2;
		    		graphics.drawString(labels[j], p, q);
		    		graphics.drawString(df.format(percentage[j]), p, q+bottom);
		    	}
		    }
		}
	}
	
	private BufferedImage zoom(BufferedImage img, double scale) {
		BufferedImage result = new BufferedImage(
				(int) (img.getWidth() * scale),
				(int) (img.getHeight() * scale), img.getType());
		Graphics2D g = result.createGraphics();
		AffineTransform at = AffineTransform.getScaleInstance(scale, scale);
		g.drawImage(img, at, null);
		return result;
	}

	private class imgView extends JLabel {
		private static final long serialVersionUID = 1L;

		public imgView(final BufferedImage img) {
			ImageIcon ii = new ImageIcon(zoom(img, 2));
			setIcon(ii);
			setHorizontalAlignment(JLabel.CENTER);
			setBorder(new EmptyBorder(10, 10, 10, 10));
		}
	}

	private class sliceList extends JPanel {

		private static final long serialVersionUID = 1L;

		public sliceList(String dir) {
			File root = new File(folderName + File.separator + dir);

			File[] files = root.listFiles(new FilenameFilter() {
				public boolean accept(File dir, String name) {
					return name.endsWith(".png");
				}
			});

			setLayout(new GridLayout(files.length, 3, 10, 10));
			
			int[] categoryCount = new int[dimensions];
			int goodnessResolution = 30;
			int[] goodness = new int[goodnessResolution];
			
			Arrays.fill( categoryCount, 0 );
			Arrays.fill( goodness, 0 );
			
			try {
			
				MainClassifierImpl classifier = new MainClassifierImpl();
			
				for (File imgName : files) {
					final BufferedImage img = ImageIO.read(imgName);
					BufferedImage result = img;
					JTextArea msgs = new JTextArea();
					msgs.setEditable(false);
					
					msgs.append( "ime slike: " + imgName.getName() + "\n" );
					
					add(new imgView(img));

					for (ImageTransformer transformer : transformers) {
						result = transformer.transform( result );
					}
					
					imgView resultView = new imgView(result);
					resultView.addMouseListener(new MouseAdapter() {
						public void mouseClicked(MouseEvent e) {
							JFrame preprocess = new JFrame("preprocessing stages");
							JPanel top = new JPanel( new GridLayout( 1, transformers.size()+1 ) );
							JPanel bottom = new JPanel( new GridLayout( 1, transformers.size()+1 ) );
							
							top.add( new JLabel( "original", JLabel.CENTER ) );
							top.add( new JLabel( "binarizer", JLabel.CENTER ) );
							top.add( new JLabel( "noise remover", JLabel.CENTER ) );
							top.add( new JLabel( "stain remover", JLabel.CENTER ) );
							top.add( new JLabel( "scaler", JLabel.CENTER ) );
							top.add( new JLabel( "line thining", JLabel.CENTER ) );
							BufferedImage curr = img;
							bottom.add( new imgView(curr) );
							for (ImageTransformer transformer : transformers) {
								curr = transformer.transform( curr );
								bottom.add( new imgView(curr) );
							}
							
							JSplitPane splitPane = new JSplitPane( JSplitPane.VERTICAL_SPLIT );
							splitPane.setDividerSize(0);
							splitPane.setDividerLocation( 30 );
							splitPane.setTopComponent(top);
							splitPane.setBottomComponent(bottom);
							preprocess.add( splitPane );
							preprocess.setVisible(true);
							preprocess.setSize( 1200, 300 );
						}});
					
					add( resultView );
					
					double[] output = classifier.classify(img, dimensions);
					int max = ClassifyUtils.getMaxIndex(output);
					double ratio = ClassifyUtils.getTwoBestRatio(output);
					
					for( int i = 0; i < dimensions; ++i ) msgs.append( String.valueOf( (int) (output[i]*100) ) + " " );
					msgs.append( "\n\n" + Category.toString(max) );
					
					++categoryCount[max];
					
					if( !classifier.isReliable( output ) ) msgs.setBackground( new Color( 0, 255, 255 ) );
					else if( !root.getName().equals( Category.toString(max) ) ) msgs.setBackground( new Color( 255, 0, 0 ) );							

					if( ratio > 10 ) ++goodness[goodnessResolution-1];
					else ++goodness[ (int) (ratio*goodnessResolution/10.) ];
					
					add( msgs );
				} 
			} catch (IOException e) {

			}
			
			String[] categories = new String[dimensions];
			
			for( int i = 0; i < dimensions; ++i ) categories[i] = Category.toString(i);
			 
			JFrame summary = new JFrame( "summary" );
			summary.setLayout( new GridLayout(2,1,10,10) );
			summary.add( new BarChart( categoryCount, categories, "klasifikacija" ) );
			summary.add( new BarChart( goodness, null, "omjer dva najjaca izlaza (1-10)" ) );
			summary.setSize(800, 800);
			summary.setVisible(true);
		}

	}

	private class runAll extends JFrame {
		private static final long serialVersionUID = 1L;
		
		private final int n = Category.CATEGORY_COUNT;
		private double[][] results = new double[n][n];
		private final String titlePrefix = "processing folder ";
		private int[] none = new int[n];
		private int[] wrong = new int[n];
		
		runAll() {
			setVisible( true );
			setSize( 600, 600 );
			
			String[] folders = new String[n+n];
			for( int i = 0; i < n; ++i ) {
				folders[i+i] = "/200dpi/" + Category.toString( i );	
				folders[i+i+1] = "/300dpi/" + Category.toString( i );
			}
			
			try {
				
				MainClassifierImpl classifier = new MainClassifierImpl();	
				
				int correct = 0, all = 0;
				
				for( int i = 0; i < n+n; ++i ) {
					setTitle( titlePrefix + i + "/" + (2*n) ); 
					File folder = new File( folderName + folders[i] );
					File[] files = folder.listFiles(new FilenameFilter() {
						public boolean accept(File dir, String name) {
							return name.endsWith(".png");
						}
					});
					
					for( File file : files ) {
						BufferedImage img = ImageIO.read(file);
						double output[] = classifier.classify(img, dimensions );
						for( int j = 0; j < dimensions; ++j ) results[i/2][j] += output[j];
						
						if( !classifier.isReliable( output ) ) ++none[i/2];
						else {
							if( ClassifyUtils.getMaxIndex(output) == i/2 ) ++correct;
							else ++wrong[i/2];
						}
						
						++all;
					}
				}
				
				for( int i = 0; i < n; ++i ) {
					double sum = 0;
					for( int j = 0; j < n; ++j ) sum += results[i][j];
					for( int j = 0; j < n; ++j ) results[i][j] /= sum;
				}

				setTitle( "drawing graph.. " );
				setLayout( new GridLayout( n+1, n+3, 1, 1 ) );
				
				add( new JLabel() );
				
				for( int i = 0; i < n; ++i ) add( new JLabel( Category.toString(i), JLabel.CENTER ) );
				add( new JLabel( "none", JLabel.CENTER ) );
				add( new JLabel( "wrong" , JLabel.CENTER ) );
				
				for( int i = 0; i < n; ++i ) {
					add( new JLabel( Category.toString( i ), JLabel.CENTER ) );
					for( int j = 0; j < n; ++j ) {
						JLabel l = new JLabel();
						int col = (int) ((1-results[i][j])*255);
						l.setSize( getWidth()/(n+1), getHeight()/(n+1) );
						l.setOpaque(true);
						l.setBackground( new Color( col, col, col ) );
						add( l );
					}
					add( new JLabel( String.valueOf( none[i] ), JLabel.CENTER ) );
					add( new JLabel( String.valueOf( wrong[i] ), JLabel.CENTER ) );
				}
				
				DecimalFormat df = new DecimalFormat( "#.##%" );
				setTitle( "overall accuracy: " + df.format( (double)correct/all ) );
			} catch( Exception e ) {
				
			}
		}
	}
	
	Tester(List<ImageTransformer> transfomers) {
		this.transformers = transfomers;

		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setTitle("tester");
		this.setSize(800, 800);

		Vector<String> dirNames = new Vector<String>();

		File root = new File(folderName);
		FileFilter filter = new FileFilter() {
			public boolean accept(File f) {
				return f.isDirectory() && !f.getName().startsWith(".");
			}
		};

		for (File dir1 : root.listFiles(filter))
			for (File dir2 : dir1.listFiles(filter))
				dirNames.add(dir1.getName() + "/" + dir2.getName());

		final JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		final JList dirList = new JList(dirNames);
				
		dirList.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					setTitle("working...");
					int index = dirList.locationToIndex(e.getPoint());
					JPanel panel = new sliceList((String) dirList.getModel()
							.getElementAt(index));
					splitPane.setRightComponent(new JScrollPane(panel));
					setTitle("tester");
				}
			}
		});

		JSplitPane left = new JSplitPane( JSplitPane.VERTICAL_SPLIT );
		JButton runAllButton = new JButton( "run all" );
   
		runAllButton.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent e ) {
				new runAll();
			}
		} );
		
		left.setTopComponent( runAllButton );
		left.setBottomComponent( dirList );
		left.setDividerSize( 0 );
		left.setDividerLocation( 50 );
		
		splitPane.setLeftComponent(left);
		splitPane.setRightComponent(new JLabel("double klikni na folder"));
		splitPane.setDividerLocation(120);
		splitPane.setDividerSize(0);
		
		add(splitPane);
		setVisible(true);
	}
}
