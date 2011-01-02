package hr.fer.nm_projekt.classifiers;

import java.io.File;

public interface Classifier {

	/**
	 * Metoda koja klasificira uzorak. Kao ulaz se primaju vrijednosti uzorka (u
	 * varijabli <code>input</code>) i broj dimenzija koje se očekuju kao
	 * rezultat (zapisano u varijabli <code>dimensions</code>). Rezultat
	 * klasifikacije se pohranjuje u varijablu <code>output</code> i to tako da
	 * se prvih <code>dimensions</code> elemenata polja popuni s vrijednostima
	 * od 0.0 do 1.0 koje predstavljaju vjerojatnosti za određeni izlaz. Suma
	 * tih vrijednosti mora biti jednaka 1.0. Vrijednosti polja
	 * <code>output</code> za indekse veće od <code>dimensions</code> se trebaju
	 * ignorirati.
	 * 
	 * @param input
	 *            Polje realnih brojeva koji predstavljaju uzorak
	 * @param dimensions
	 *            Broj korištenih dimenzija u izlazu.
	 * @param output
	 *            Polje koje predstavlja izlaz klasifikatora - vjerojatnosti
	 *            pojedinih korištenih izlaza.
	 */
	public void classify(double[] input, int dimensions, double[] output);
	
	public void saveTo(File file);
	
	public void loadFrom(File file);

}
