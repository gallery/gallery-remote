package HTTPClient;

/**
 * Simple listener to provide feedback to the user interface about
 * the ongoing http transfer
 * @author Markus Cozowicz (mc@austrian-mint.at)
 **/

public interface TransferListener {
	public void dataTransferred(int transferred, int overall, double kbPerSecond);

	public void transferStart(int overall);
	public void transferEnd();
}