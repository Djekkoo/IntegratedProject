package main;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/** 
 * Handles the preparation of files for being sent over the network.
 * Also handles writing operations for file packets received over the network.
 * 
 * @author      Joey Haas <j.haas@student.utwente.nl>
 * @version     0.1
 * @since       2014-04-10
 */
public class FileTransferHandler {

	/**
	 * The file to do read/write operations on.
	 */
	private RandomAccessFile f;
	
	public FileTransferHandler() throws FileNotFoundException {
		f = new RandomAccessFile("/dev/null", "rw");
	}
	
	public FileTransferHandler(String filename, String mode) throws FileNotFoundException {
		f = new RandomAccessFile(filename, mode);
	}
	
	/**
	 * Retrieves the packet(Checksum+Filedata)
	 * 
	 * @return The packet, ready to be sent
	 */
	public byte[] getPacket() {
		try {
			MessageDigest md5 = MessageDigest.getInstance("MD5");
            long longLength = f.length();
            int dataLength = (int) longLength;
            if (dataLength != longLength)
                throw new IOException("File size >= 2 GB");
            byte[] data = new byte[dataLength];
            f.readFully(data);
            
            byte[] checksum = md5.digest(data);
            byte[] packet = new byte[checksum.length + dataLength];
            System.arraycopy(checksum, 0, packet, 0, 16);
            System.arraycopy(data, 0, packet, 16, data.length);
            
            return packet;
		} catch (NoSuchAlgorithmException | IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Parses a file packet, compares the checksum and returns the file data.
	 * 
	 * @param 	packet The file packet to be parsed
	 * @return 	The file data contained by the packet.
	 * @throws 	IOException If checksum is not correct
	 */
	public byte[] parsePacket(byte[] packet) throws IOException {
		try {
			MessageDigest md5 = MessageDigest.getInstance("MD5");
			
            
            byte[] checksum = new byte[16];
            byte[] data = new byte[packet.length-16];
			
			System.arraycopy(packet, 0, checksum, 0, 16);
            System.arraycopy(packet, 16, data, 0, data.length);
            
            byte[] checksumCompare = md5.digest(data);
            
            boolean checksumMatch = true;
            for(int i = 0; i < 16; i++) {
            	if(checksum[i] != checksumCompare[i]) {
            		checksumMatch = false;
            	}
            }
            if(!checksumMatch) {
            	throw new IOException("Checksum error");
            }
            return data;
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Writes an entire byte array to the file.
	 * 
	 * @param 	data The byte array to write.
	 * @throws 	IOException
	 */
	public void writeFile(byte[] data) throws IOException {
		f.write(data);
	}
	
	/**
	 * Closes the old file and sets the new file to do all future I/O operations with.
	 * 
	 * @param 	filename The file location
	 * @param	mode The mode with which to open the file, e.g. read, readwrite.
	 * @throws 	IOException
	 */
	public void setFile(String filename, String mode) {
		try {
			f.close();
			f = new RandomAccessFile(filename, mode);	
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
