package hr.fer.nm_projekt.utilities;

import hr.fer.nm_projekt.preprocessing.BlankImage;
import hr.fer.nm_projekt.preprocessing.ImageBinarizer;
import hr.fer.nm_projekt.preprocessing.ImageLineThinning;
import hr.fer.nm_projekt.preprocessing.ImageNoiseRemover;
import hr.fer.nm_projekt.preprocessing.ImageScaler;
import hr.fer.nm_projekt.preprocessing.ImageStainRemover;
import hr.fer.nm_projekt.preprocessing.ImageTransformer;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

/**
 * Koristi se slièno kao tester za pretprocesiranje. S desne strane, duplim
 * klikom, odabere se direktorij, stave kvaèice na slike koje æe se koristiti
 * i na dnu lupi "Spremi". Takoðer, na dnu je i gumb za invertiranje odabira.
 * 
 * @author EmP
 *
 */
public class ImageSelector extends JFrame {

	private static final long serialVersionUID = 1L;
	private static String folderName = "data";
	private static String selectionsFile = "selected images.txt";

	private List<ImageTransformer> transformers;
	private Map<String, Set<String>> selectedImages;

	public static void main(String[] args) throws IOException {
		List<ImageTransformer> transformers = new ArrayList<ImageTransformer>();

		transformers.add(new ImageBinarizer());
		transformers.add(new ImageNoiseRemover());
		transformers.add(new ImageStainRemover());
		transformers.add(new ImageScaler(50, 50));
		transformers.add(new ImageLineThinning());
		
		new ImageSelector(transformers);
	}

	private ImageSelector(List<ImageTransformer> transfomers) throws IOException {
		this.transformers = transfomers;
		
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setTitle("Odabir slika");
		this.setSize(600, 600);
		loadSelection();

		Vector<String> dirNames = new Vector<String>();

		File root = new File(folderName);
		FileFilter filter = new FileFilter() {
			public boolean accept(File f) {
				return f.isDirectory() && !f.getName().startsWith(".");
			}
		};

		for (File dir1 : root.listFiles(filter))
			for (File dir2 : dir1.listFiles(filter))
				dirNames.add(dir1.getName() + File.separator + dir2.getName());

		final JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		final JList dirList = new JList(dirNames);

		dirList.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					setTitle("working...");
					int index = dirList.locationToIndex(e.getPoint());
					JPanel panel = new ImageListPanel((String) dirList
							.getModel().getElementAt(index));
					splitPane.setRightComponent(new JScrollPane(panel));
					setTitle("tester");
				}
			}
		});

		splitPane.setLeftComponent(dirList);
		splitPane.setRightComponent(new JLabel("double klikni na folder"));
		splitPane.setDividerLocation(100);
		add(splitPane);

		setVisible(true);
	}

	private void loadSelection() throws IOException {
		selectedImages = new HashMap<String, Set<String>>();
		File inputFile = new File(folderName + "/" + selectionsFile);

		if (inputFile.exists()) {
			try {
				BufferedReader reader = new BufferedReader(new FileReader(
						inputFile));

				try {
					while (true) {
						String folder = reader.readLine();
						int fileCount = Integer.parseInt(reader.readLine());
						Set<String> files = new HashSet<String>();
						for (int i = 0; i < fileCount; i++)
							files.add(reader.readLine().trim());
						selectedImages.put(folder, files);
					}
				} catch (Exception ioex) {
					try {
						reader.close();
					} catch (Exception ex) {
					}
				}
			} catch (FileNotFoundException e) {

			}
		} else
			inputFile.createNewFile();
	}

	private void saveSelection(String dir, List<String> files)
			throws IOException {
		if (selectedImages.containsKey(dir))
			selectedImages.remove(dir);
		selectedImages.put(dir, new HashSet<String>(files));

		File inputFile = new File(folderName + "/" + selectionsFile);
		try {
			BufferedWriter writer = new BufferedWriter(
					new FileWriter(inputFile));

			for (String folder : selectedImages.keySet()) {
				writer.write(folder);
				writer.newLine();

				Integer count = selectedImages.get(folder).size();
				writer.write(count.toString());
				writer.newLine();

				for (String fileName : selectedImages.get(folder)) {
					writer.write(fileName);
					writer.newLine();
				}
			}
			writer.close();
		} catch (FileNotFoundException e) {

		}
	}

	private class ImageListPanel extends JPanel {
		private static final long serialVersionUID = 1L;

		private List<JCheckBox> checks;
		private String directory;
		private List<String> fileNames;

		public ImageListPanel(String dir) {
			this.checks = new ArrayList<JCheckBox>();
			this.directory = dir;
			this.fileNames = new ArrayList<String>();

			File root = new File(folderName + File.separator + dir);

			File[] files = root.listFiles(new FilenameFilter() {
				public boolean accept(File dir, String name) {
					return name.endsWith(".png");
				}
			});

			setLayout(new GridLayout(files.length + 1, 4));

			for (File imgName : files) {
				try {
					JCheckBox chBox = new JCheckBox();
					this.checks.add(chBox);
					this.fileNames.add(imgName.getName());
					
					if (selectedImages.containsKey(dir))
						if (selectedImages.get(dir).contains(imgName.getName()))
							chBox.setSelected(true);
					this.add(chBox);

					BufferedImage img = ImageIO.read(imgName);
					JLabel lblImage = new JLabel(new ImageIcon(img));
					this.add(lblImage);
					
					int counter = 0;
					boolean isBlank = false;

					BlankImage bImage = new BlankImage(0.995);
					for (ImageTransformer transformer : transformers) {
						if (isBlank == false) img = transformer.transform(img);
						if (counter == 0) isBlank = bImage.isBlank(img);
						counter++;
					}
					this.add(new JLabel(new ImageIcon(img)));

					add(new JLabel(imgName.getName(), JLabel.LEFT));
				} catch (IOException e) {

				}
			}

			JButton btnInvert = new JButton("Invertiraj odabir");
			btnInvert.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					for (JCheckBox chBox : checks)
						chBox.setSelected(!chBox.isSelected());
				}
			});
			add(btnInvert);

			JButton btnSave = new JButton("Spremi");
			btnSave.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					List<String> files = new ArrayList<String>();
					for (int i = 0; i < checks.size(); i++)
						if (checks.get(i).isSelected())
							files.add(fileNames.get(i));
					try {
						saveSelection(directory, files);
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
			});
			add(btnSave);
			add(new JLabel());
		}
	}
}
