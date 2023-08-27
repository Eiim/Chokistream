package chokistream;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class WritableInputStream {
	
	private boolean finished = false;
	private int length = 0;
	private byte[] contents = new byte[1];

	/**
	 * Create a new WritableInputStream.
	 */
	public WritableInputStream() {
		// Left blank but must exist.
	}
	
	/**
	 * Create a new WritableInputStream with existing data, and tell it
	 * if the data is already finished or not.
	 * @param data Data to store.
	 * @param isFinished If the data is already finished.
	 */
	public WritableInputStream(byte[] data, boolean isFinished) {
		finished = isFinished;
		length = data.length;
		contents = data;
	}
	
	/**
	 * Writes a byte to the ImageInputStream.
	 * @param data The data to write.
	 */
	public void write(byte data) throws IOException {
		if (finished) throw new IOException("Stream finished, cannot write");
		// Copy the data
		contents[length] = data;
		length++;
		// Double the length of the array and copy contents if the array is full
		if (length >= contents.length) {
			byte[] tmpContents = new byte[contents.length * 2];
			for (int i = 0; i < contents.length; i++) {
				tmpContents[i] = contents[i];
			}
			contents = tmpContents;
		}
	}
	
	/**
	 * Writes an array of bytes to the ImageInputStream.
	 * @param data The data to write.
	 */
	public void write(byte[] data) throws IOException {
		if (finished) throw new IOException("Stream finished, cannot write");
		int newLength = length + data.length;
		if (newLength >= contents.length) {
			// Keep doubling the size of the array until it is at least 2x larger than data.length
			int newContentsSize = contents.length * 2;
			while (newContentsSize <= newLength) {
				newContentsSize *= 2;
			}
			byte[] tmpContents = new byte[newContentsSize];
			// Copy over the contents of the old array
			for (int i = 0; i < contents.length; i++) {
				tmpContents[i] = contents[i];
			}
			contents = tmpContents;
		}
		// Copy the data
		for (int i = 0; i < data.length; i++) {
			contents[length + i] = data[i];
		}
		length += data.length;
	}
	
	/**
	 * Mark the WritableInputStream as finished for writing.
	 */
	public void markFinishedWriting() throws IOException {
		if (finished) throw new IOException("Stream finished, cannot finish again");
		finished = true;
	}
	
	/**
	 * Get an InputStream from this WritableInputStream's contents.
	 * @return InputStream created from this WritableInputStream's contents
	 * @throws IOException
	 */
	public InputStream getInputStream() throws IOException {
		if (!finished) throw new IOException("Stream not finished, cannot read until finished");
		return new ByteArrayInputStream(contents);
	}
	
}
