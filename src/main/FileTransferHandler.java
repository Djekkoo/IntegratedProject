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
	private RandomAccessFile currentFile;
	/**
	 * The filename of the current file.
	 */
	private String currentFilename;
	
	public FileTransferHandler() throws FileNotFoundException {
		currentFile = new RandomAccessFile("/dev/null", "rw");
		currentFilename = "null";
	}
	
	public FileTransferHandler(String filename, String mode) throws FileNotFoundException {
		currentFile = new RandomAccessFile(filename, mode);
		String[] splitFilename = filename.split("/");
		currentFilename = splitFilename[splitFilename.length-1];
	}
	
	public static void main(String[] args) {
		FileTransferHandler fthr;
		try {
			fthr = new FileTransferHandler("/home/joeyjo0/test","r");
			FileTransferHandler fthw = new FileTransferHandler("/home/joeyjo0/test3","rw");
			fthw.writeFile(fthw.parsePacket(fthr.getPacket()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Retrieves the packet(Checksum+Filedata)
	 * 
	 * @return The packet, ready to be sent
	 */
	public byte[] getPacket() {
		try {
			MessageDigest md5 = MessageDigest.getInstance("MD5");
            long longLength = currentFile.length();
            int dataLength = (int) longLength;
            if (dataLength != longLength)
                throw new IOException("File size >= 2 GB");
            byte[] data = new byte[dataLength];
            currentFile.readFully(data);
            
            byte[] preamble = "FILE ".getBytes();
            byte[] checksum = md5.digest(data);
            
            byte[] filename = currentFilename.getBytes();
            if(filename.length > 255) {
            	throw new IOException("File name >= 256 Bytes");
            }
            byte filenameLength = (byte)(filename.length&0xFF);
            
            byte[] packet = new byte[checksum.length + dataLength + filenameLength + 1 + 5];
            int offset = 0;
            System.arraycopy(preamble, 0, packet, 0, 5);
            offset += 5;
            System.arraycopy(checksum, 0, packet, offset, 16);
            offset += 16;
            System.arraycopy(new byte[]{filenameLength}, 0, packet, offset, 1);
            offset += 1;
            System.arraycopy(filename, 0, packet, offset, filenameLength);
            offset += filenameLength;
            System.arraycopy(data, 0, packet, offset, data.length);
            
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
			byte[] preamble = new byte[5]; //Always "DATA "
            byte[] checksum = new byte[16];
            
            byte filenameLength = packet[5+16];
            byte[] filename = new byte[filenameLength];
            byte[] data = new byte[packet.length-(16+5+1+filenameLength)];
            
            int offset = 0;
            System.arraycopy(packet, 0, preamble, 0, 5);
            offset += 5;
			System.arraycopy(packet, offset, checksum, 0, 16);
			offset += 16 + 1; //Because of the filenameLength byte.
			System.arraycopy(packet, offset, filename, 0, filenameLength);
			offset += filenameLength;
            System.arraycopy(packet, offset, data, 0, data.length);
            
            System.out.println("\'" + new String(filename) + "\' file size: " + data.length);
            
            byte[] preambleCompare = "FILE ".getBytes();
            boolean preambleMatch = true;
            for(int i = 0; i < 5; i++) {
            	if(preamble[i] != preambleCompare[i]) {
            		preambleMatch = false;
            	}
            }
            if(!preambleMatch) {
            	throw new IOException("Incorrect preamble, should be \"FILE \".");
            }
            byte[] checksumCompare = md5.digest(data);
            boolean checksumMatch = true;
            for(int i = 0; i < 16; i++) {
            	if(checksum[i] != checksumCompare[i]) {
            		checksumMatch = false;
            	}
            }
            if(!checksumMatch) {
            	throw new IOException("Checksum error; checksum incorrect.");
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
		currentFile.setLength(0);
		currentFile.write(data);
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
			currentFile.close();
			currentFile = new RandomAccessFile(filename, mode);	
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
