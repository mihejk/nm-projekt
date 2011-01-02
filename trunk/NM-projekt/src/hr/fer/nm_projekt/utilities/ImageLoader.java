package hr.fer.nm_projekt.utilities;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;

/**
 * Aplikacija za izvlačenje polja iz slike skeniranog obrasca za ispite. Pokreće
 * se tako da se kao argument pošalje putanja prema direktoriju koji sadrži
 * slike u jpg formatu. Program otvara po jedan prozor za svaku pronađenu sliku
 * unutar direktorija. Unutar prozora je potrebno kliknuti vanjske rubove crnih
 * kvadrata na rubovima skeniranog dokumenta. Potrebno ih je odabrati ovim
 * redoslijedom: gornji lijevi, gornji desni, donji desni i donji lijevi
 * kvadrat. Nakon što su odabrani kvadrati, na desnoj strani prozora bi se
 * trebale prikazati slike izrezanih polja. Kako bi se dobivene slike pohranile,
 * potrebno je pritisnuti gumb "Save slices". Slike se pohranjuju u
 * poddirektorij <code>slices</code> i to tako da su nazvane u obliku
 * <code>imeSlike-3-2.png</code> što označava polje trećeg reda i drugog stupca.
 * 
 * @author mihej
 * 
 */
public class ImageLoader extends JFrame {
	
	private static final long serialVersionUID = 1L;
	private final SliceView sliceView[][] = new SliceView[20][3];

	public ImageLoader(final File file) {
		setTitle("Master");
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setSize(950, 650);
		setLocation(10, 15);
		
		final JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		
		JPanel panel = new JPanel(new GridLayout(20, 3));
		for (int i = 0; i < 20; i++) {
			for (int j = 0; j < 3; j++) {
				sliceView[i][j] = new SliceView();
				panel.add(new JScrollPane(sliceView[i][j]));
			}
		}
		
		JButton button = new JButton("Save slices");
		button.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				for (int i = 0; i < 20; i++) {
					for (int j = 0; j < 3; j++) {
						int tmp = file.getName().lastIndexOf('.');
						String filename = file.getParent() + "/slices/" + file.getName().substring(0, tmp) + "-" + (i + 1) + "-" + (j + 1) + ".png";
						File newFile = new File(filename);
						try {
							ImageIO.write(sliceView[i][j].getImage(), "png", newFile);
						} catch (IOException e1) {
							e1.printStackTrace();
						}
					}
				}
			}
		});
		
		JPanel rightPanel = new JPanel(new BorderLayout());
		rightPanel.add(new JScrollPane(panel), BorderLayout.CENTER);
		rightPanel.add(button, BorderLayout.SOUTH);
		
		splitPane.setRightComponent(rightPanel);
		
		
		splitPane.setDividerLocation(800);
		try {
			BufferedImage image = ImageIO.read(file);
			final ImageView imageView = new ImageView();
			imageView.setImage(image);
			imageView.addMouseListener(new MouseAdapter() {
				
				public void mousePressed(MouseEvent e) {
					if (imageView.setPoint(new Point(e.getX(), e.getY()))) {
						imageView.calculateSlices();
						int dividerLocation = splitPane.getDividerLocation();
						if (dividerLocation % 2 == 0) {
							splitPane.setDividerLocation(dividerLocation + 1);
						} else {
							splitPane.setDividerLocation(dividerLocation - 1); 
						}
						
					}
					imageView.repaint();
				}
				
			});
			splitPane.setLeftComponent(new JScrollPane(imageView));
		} catch (IOException e) {
			e.printStackTrace();
		}
		getContentPane().add(splitPane);
		setVisible(true);
	}
	
	private class ImageView extends JComponent {
		
		private static final long serialVersionUID = 1L;
		private BufferedImage image;
		private Point2D[] points = new Point2D[4];
		private int pointsClicked = 0;
		private Dimension preferredSize = new Dimension(0, 0);
		private ImageObserver imageObserver = new ImageObserver() {
			
			@Override
			public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height) {
				return false;
			}
		};
		
		public void setImage(BufferedImage image) {
			this.image = image;
			preferredSize = new Dimension(image.getWidth(), image.getHeight());
		}
		
		public boolean setPoint(Point2D point) {
			if (pointsClicked >= points.length) {
				pointsClicked = 0;
			}
			points[pointsClicked] = point;
			pointsClicked++;
			return pointsClicked == 4;
		}
		
		public void calculateSlices() {
			double deg1 = Math.atan2(points[0].getY() - points[1].getY(), points[1].getX() - points[0].getX());
			double deg2 = Math.atan2(points[2].getX() - points[1].getX(), points[2].getY() - points[1].getY());
			double deg3 = Math.atan2(points[3].getY() - points[2].getY(), points[2].getX() - points[3].getX());
			double deg4 = Math.atan2(points[3].getX() - points[0].getX(), points[3].getY() - points[0].getY());
			
			double deg = (deg1 + deg2 + deg3 + deg4) / 4.0;
			
			AffineTransform at = new AffineTransform();
			at.rotate(deg);
			AffineTransformOp ato = new AffineTransformOp(at, AffineTransformOp.TYPE_BICUBIC); 
			Rectangle2D rectangle = ato.getBounds2D(image);
			BufferedImage newImg = new BufferedImage((int) rectangle.getWidth(), (int) rectangle.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
			
			ato.filter(image, newImg);
			preferredSize = new Dimension(newImg.getWidth(), newImg.getHeight());
			
			Point2D newPoints[] = new Point2D[4];
			at.transform(points, 0, newPoints, 0, 4);
			
			double formWidth = ((newPoints[1].getX() - newPoints[0].getX()) + (newPoints[2].getX() - newPoints[3].getX())) / 2.0;
			double formHeight = ((newPoints[3].getY() - newPoints[0].getY()) + (newPoints[2].getY() - newPoints[1].getY())) / 2.0;	
			double boxWidth = 0.1275 * formWidth;
			double sliceWidth = boxWidth / 3.0;
			double boxHeight = 0.571 * formHeight;
			double sliceHeight = boxHeight / 20.0;
			double boxX = 0.5535 * formWidth + newPoints[0].getX();
			double boxY = 0.3135 * formHeight + newPoints[0].getY();
			for (int i = 0; i < 20; i++) {
				for (int j = 0; j < 3; j++) {
					WritableRaster raster = newImg.getRaster().createWritableChild((int) (boxX + sliceWidth * j), (int) (boxY + sliceHeight * i), (int) sliceWidth, (int) sliceHeight, 0, 0, null);
					BufferedImage slice = new BufferedImage(newImg.getColorModel(), raster, false, null);
					sliceView[i][j].setImage(slice);
				}
			}
		}
		
		@Override
		public Dimension getPreferredSize() {
			return preferredSize;
		}
		
		@Override
		public void paint(Graphics g) {
			g.drawImage(image, 0, 0, imageObserver);
			g.setColor(Color.RED);
			if (pointsClicked > 1) {
				g.drawLine((int) points[0].getX(), (int) points[0].getY(), (int) points[1].getX(), (int) points[1].getY());
			}
			if (pointsClicked > 2) {
				g.drawLine((int) points[1].getX(), (int) points[1].getY(), (int) points[2].getX(), (int) points[2].getY());
			}
			if (pointsClicked == 4) {
				g.drawLine((int) points[2].getX(), (int) points[2].getY(), (int) points[3].getX(), (int) points[3].getY());
				g.drawLine((int) points[3].getX(), (int) points[3].getY(), (int) points[0].getX(), (int) points[0].getY());
			}
			super.paint(g);
		}
		
	}
	
	private class SliceView extends JComponent {
		
		private static final long serialVersionUID = 1L;
		private BufferedImage image;
		private Dimension preferredSize = new Dimension(0, 0);
		private ImageObserver imageObserver = new ImageObserver() {
			
			@Override
			public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height) {
				return false;
			}
		};
		
		public void setImage(BufferedImage image) {
			this.image = image;
			preferredSize = new Dimension(image.getWidth(), image.getHeight());
			repaint();
		}
		
		public BufferedImage getImage() {
			return image;
		}
		
		@Override
		public Dimension getPreferredSize() {
			return preferredSize;
		}
		
		@Override
		public void paint(Graphics g) {
			if (image != null) {
				g.drawImage(image, 0, 0, imageObserver);
			}
		}
		
	}

	public static void main(String[] args) throws IOException {
		final File directory = new File(args[0]);
		if (!directory.exists()) {
			System.out.println("Doesn't exist");
			return;
		}
		if (!directory.isDirectory()) {
			System.out.println("Not a directory");
			return;
		}
		
		File slicesDirectory = new File(directory.getPath() + "/slices");
		slicesDirectory.mkdir();
		
		File[] files = directory.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".jpg") || name.endsWith(".jpeg");
			}
		});
		
		for (final File file : files) {
			SwingUtilities.invokeLater(new Runnable() {
				
				@Override
				public void run() {
					new ImageLoader(file);
				}
			});
		}
	}

}
