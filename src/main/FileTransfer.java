package main;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

/** 
 * @author      Joey Haas <j.haas@student.utwente.nl>
 * @version     0.1
 * @since       2014-04-10
 */
public class FileTransfer {

	FileInputStream in;
	FileOutputStream out;
	public FileTransfer(boolean transmit, String filename) {
		try {
		
			// TODO Read file from filename.
			if(transmit) {
				in = new FileInputStream(filename);
			} else {
				out = new FileOutputStream(filename);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
}
