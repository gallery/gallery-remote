package HTTPClient;

/**
 * Simple transferListener to provide feedback to the user interface about
 * the ongoing http transfer
 * @author Markus Cozowicz (mc@austrian-mint.at)
 **/

public interface TransferListener {
	public void dataTransferred(int transferredThisFile, int sizeThisFile, double kbPerSecond);

	public void transferStart(int sizeThisFile);
	public void transferEnd(int sizeThisFile);
}