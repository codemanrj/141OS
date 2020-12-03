/*
Author: Raul Montano Jr

Reference for FILE I/O : 
https://beginnersbook.com/2014/01/how-to-write-to-file-in-java-using-bufferedwriter/
*/

import java.lang.Math;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class OS141 {
	
	int numUsers = 0;
	int numDisks = 0; 
	int numPrinters = 0;
	String userNames[];
	UserThread users[];
	Printer printers[];
	Disk disks[];
	//PrinterManager pm;
	//DiscManager dm;

	OS141(String [] args)
	{
		configure(args);	

		users = new UserThread[numUsers];
		for(int i=0; i<numUsers; i++) {
			users[i] = new UserThread(i+1);
		}
		disks = new Disk[numDisks];
		for(int i=0; i<numDisks; i++) {
			disks[i] = new Disk();
		}
		printers = new Printer[numPrinters];
		for(int i=0; i<numPrinters; i++) {
			printers[i] = new Printer(i+1);
		}
	}
	void configure(String[] argv)
	{
		for(int i=0; i< argv.length; i++)
			System.out.println(argv[i]);

		numUsers = Math.abs(Integer.parseInt(argv[0]));
		numPrinters = Math.abs(Integer.parseInt(argv[argv.length -1]));
		numDisks = Math.abs(Integer.parseInt(argv[argv.length-2]));

		userNames = new String[numUsers];

		for(int i = 0; i < numUsers; i++)
		{
			userNames[i] = argv[i+1];
		}

	}

	public static void main(String[] args) 
	{	
		OS141 os = new OS141(args);

		StringBuffer s = new StringBuffer("Hello from main ");
		for(Printer p : os.printers)
		{
			p.print(s);
		}


	
	/*	
		for(UserThread user : os.users)
		{
			user.start();
		}
	*/
	}
}

class Disk {
	static final int NUM_SECTORS = 1024;
	StringBuffer sectors[] = new StringBuffer[NUM_SECTORS];
	int capacity;
	
	Disk()
	{
		for(int i=0; i<1024; i++)
		{
			sectors[i] = new StringBuffer(capacity);
		}
		System.out.println("Created a disk ");
	}
	void write(int sector, StringBuffer data) throws InterruptedException {
		for(int i =0; i < data.capacity(); i++)
		{
			sectors[sector].append(data.charAt(i));
		}
		Thread.sleep(200);
	}  // call sleep
	StringBuffer read(int sector, StringBuffer data) throws InterruptedException {
		for(int i=0; i<capacity; i++) 
		{
			data.append(sectors[sector].charAt(i));
		}
		Thread.sleep(200);
		
		return data;
		
	}   // call sleep
}

class Printer
{
	int id;
	String name;
	BufferedWriter out = null;
	
	Printer(int id)
	{
		this.id = id;
		this.name = "PRINTER" + Integer.toString(id);
		System.out.println("Created a printer ");
	}
	
	
	void print(StringBuffer b)
	{
		String s = "";

		for(int i=0; i <b.length(); i++)
		{
			s = s + b.charAt(i);
		}
			try {
				File file = new File(name);
				if (!file.exists()) {
	     			file.createNewFile();
	  			}

				FileWriter fw = new FileWriter(file);
				out = new BufferedWriter(fw);
				out.write(s, 0, b.length());
				System.out.println("Success");
				} catch (IOException e) {
					e.printStackTrace();
				} finally
					{ 
					try{
						if(out!=null)
						out.close();
					}catch(Exception ex){
						System.out.println("Error in closing the BufferedWriter"+ex);
						}
					}
			try {
				Thread.sleep(2750);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}	
}

class UserThread extends Thread {
	
	private StringBuffer line;
	private String fileName;
	File infile;
	Scanner in;
	//Constructor
	UserThread(int id){
		this.line = new StringBuffer();
		String fname = "USER" + Integer.toString(id);
		
		System.out.println("Created a user");
	}
	
/*	void saveToDisk() {
		try {
			infile = new File(fileName);
			in = new Scanner(infile);
		} catch (FileNotFoundException e) {
			System.out.println("User file not found ");
		}
	}
	
	void requestPrint() {
	}
*/

}
