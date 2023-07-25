package chokistream;

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
		return new CustomInputStream(contents, length);
	}
	
	private class CustomInputStream extends InputStream {
		
		private boolean closed = false;
		private int length = 0;
		private int position = 0;
		private boolean marked = false;
		private int markedPosition = 0;
		private int markedReadLimit = 0;
		private byte[] contents = new byte[1];
		
		/**
		 * Create a CustomInputStream with content.
		 * @param _contents The data to load.
		 * @param _length The length of the data.
		 */
		public CustomInputStream(byte[] _contents, int _length) {
			contents = _contents;
			length = _length;
		}
		
		@Override
		public int available() throws IOException {
			if (closed) throw new IOException("Stream closed");
			return length - position;
		}
		
		@Override
		public void close() throws IOException {
			if (closed) throw new IOException("Stream closed");
			closed = true;
			length = 0;
			position = 0;
			marked = false;
			markedPosition = 0;
			markedReadLimit = 0;
			contents = new byte[0];
		}
		
		@Override
		public void mark(int readLimit) {
			marked = true;
			markedPosition = position;
			markedReadLimit = readLimit;
		}
		
		@Override
		public boolean markSupported() {
			return true;
		}

		@Override
		public int read() throws IOException {
			if (closed) throw new IOException("Stream closed");
			int retVal = -1;
			if (position < length) {
				retVal = contents[position];
				position++;
			}
			return retVal;
		}
		
		@Override
		public int read(byte[] b) throws IOException {
			if (closed) throw new IOException("Stream closed");
			int bytesRead = 0;
			if (position == length) {
				bytesRead = -1;
			} else {
				for (int i = 0; i < b.length && position + i < length; i++) {
					b[i] = contents[position + i];
					bytesRead++;
				}
				position += bytesRead;
			}
			return bytesRead;
		}
		
		@Override
		public int read(byte[] b, int off, int len) throws IOException {
			if (closed) throw new IOException("Stream closed");
			int bytesRead = 0;
			if (position == length) {
				bytesRead = -1;
			} else {
				for (int i = 0; i < len && position + i < length; i++) {
					b[off + i] = contents[position + i];
					bytesRead++;
				}
				position += bytesRead;
			}
			return bytesRead;
		}
		
		@Override
		public void reset() throws IOException {
			if (closed) throw new IOException("Stream closed");
			if (marked) {
				if (position - markedPosition > markedReadLimit) {
					position = markedPosition;
				} else {
					throw new IOException("Mark invalidated");
				}
			} else {
				throw new IOException("Stream not marked");
			}
		}
		
		@Override
		public long skip(long n) throws IOException {
			long bytesToSkip = n;
			if (position + n > length) {
				bytesToSkip = length - position;
			}
			position += bytesToSkip;
			return bytesToSkip;
		}

	}
	
}
