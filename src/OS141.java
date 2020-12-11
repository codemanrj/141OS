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
	DiscManager dm;

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

		dm = new DiskManager(numDisks);
	}
	void configure(String[] argv)
	{
		for(int i=0; i< argv.length; i++)
			System.out.println(argv[i]);

		int argIndex = 0;
		numUsers = Math.abs(Integer.parseInt(argv[argIndex]));
		argIndex = argIndex + numUsers + 1;
		numDisks = Math.abs(Integer.parseInt(argv[argIndex]));
		argIndex++;
		numPrinters = Math.abs(Integer.parseInt(argv[argIndex]));
		

		userNames = new String[numUsers];

		for(int i = 0; i < numUsers; i++)
		{
			userNames[i] = argv[i+1];
		}

	}

	public static void main(String[] args) 
	{	
		OS141 os = new OS141(args);

		for(UserThread user : os.users)
		{
			user.start();
			user.read();
		}

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
	File inputFile;
	Scanner in;

	UserThread(int id){
		this.line = new StringBuffer();
		fileName = "USER" + Integer.toString(id);
		inputFile = new File(fileName);
		in = new Scanner(inputFile);

		System.out.println("Created a user");
	}
	
	void read(){
		
		if(in.hasNextLine())
		{
			line.append(in.nextLine());
			interpretLine();
		}

	}

	void interpretLine(){
		int i = 0;
		String fileName;
		if(line.charAt(i) == '.')
		{
			i++;
			if(line.charAt(i) == 's')
			{
				saveToDisk();
			}
			else{ //it is a 'p'
				requestPrint();
			}
		}
	}

	//the line in buffer contains either save or print
	//and the name of the file 
	//i is the index where the name starts on line
	String getFileName(int i)
	{
		 return line.substring(i);
	}

	void saveToDisk() {
		int diskToUse = dm.request(); //request the next available disk
		int sector = disks[diskToUse].getNextFreeSector();

		int i = 0;
		while(line.charAt(i) != '\0')
		{
			i++;
		}

		String fname = getFileName(i+1); //this name will be entered in to Directory Manager
									   // as a string buffer

		//clear the string buffer
		line.delete(0, line.length());

		//put the next line in it 
		// while the second characte of this line isn't e for .end
		// I want to save this line to the disk...
		line.append(in.nextLine);

		while(line.charAt[1] != 'e')
		{
			disks[diskToUse].write(sector, line);
			
			//clear line
			line.delete(0, line.length());

			//append the next
			line.append(in.nextLine);
		}

		if(line.charAt[1] != 'e')
		{
			line.delete(0, line.length());
		}


		//furthermore, i need to create a FileInfo object for this file
		read();
	}
	void requestPrint() {
		PrintJobThread p = new PrintJobThread();
		p.start();
	}
}

class FileInfo{
	int diskNumber;
	int startingSector;
	int fileLength;
	
	FileInfo(int disk, int sector, int length)
	{
		diskNumber = disk;
		startingSector = sector;
		fileLength = length;
	}
}


class DirectoryManager {
	private Hashtable<String, FileInfo> T 

	DirectoryManager()
	{
		T = new Hashtable<String, FileInfo>();
	}

	void enter(StringBuffer fileName, FileInfo file){
		T.put(fileName, file);
	}
	FileInfo lookup(StringBuffer fileName)
	{
		FileInfo data = T.get(fileName);
		if(i != null)
		{
			return data;
		}
		else{
			return 0;
		}
	}
}


class ResourceManager {
	boolean isFree[];
	ResourceManager(int numberOfItems) {
		isFree = new boolean[numberOfItems];
		for (int i=0; i<isFree.length; ++i)
			isFree[i] = true;
	}
	synchronized int request() {
		while (true) {
			for (int i = 0; i < isFree.length; ++i)
				if ( isFree[i] ) {
					isFree[i] = false;
					return i;
				}
			this.wait(); // block until someone releases Resource
		}
	}
	synchronized void release( int index ) {
		isFree[index] = true;
		this.notify(); // let a blocked thread run
	}
}

class DiskManager extends ResourceManager {
  //also keeps track of the next free sector on each disk, 
  //which is useful for saving files. 
  //The DiskManager should contain the DirectoryManager for finding file sectors on Disk.
  int nextFreeSector[];

  DiskManager(int numberOfItems)
  {
	  super(numberOfItems);
	  nextFreeSector = new int[numberOfItems];
	  for(int i = 0; i < numberOfItems; i++)
	  {
		  nextFreeSector[i] = 0;
	  }
  }

	//should I override request to also return the sector or make a new method to return nextfreesector i
  int getNextFreeSector(int i)
  {
	  return nextFreeSector[i];
  }


	//modified to update and keep track of the next free sector
	synchronized void release( int index, int sectorsUsed ) {
		isFree[index] = true;
		nextFreeSector[index] += sectorsUsed;
		this.notify(); // let a blocked thread run
	}

}

class PrinterManager extends ResourceManager {
	PrinterManager(int n){
		super(n);
	}
}

class PrintJobThread extends Thread{

}