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
import java.util.Hashtable;

public class OS141 {
	int numUsers = 0;
	int numDisks = 0; 
	int numPrinters = 0;
	String userNames[];
	UserThread users[];
	Printer printers[];
	Disk disks[];
	PrinterManager pm;
	DiskManager dm;
	DirectoryManager dirm;

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

		pm = new PrinterManager(numPrinters);
		dm = new DiskManager(numDisks);
		dirm = new DirectoryManager();
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
		}

	}
	
	class UserThread extends Thread {
		
		private StringBuffer line;
		private String fileName;
		File inputFile;
		Scanner in;

		UserThread(int id){
			this.line = new StringBuffer();
			fileName = "inputs/USER" + Integer.toString(id);
			inputFile = new File(fileName);
			try {
				in = new Scanner(inputFile);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("Created a user");
		}

		@Override
		public void run(){
			read();
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
			if(line.charAt(i) == '.')
			{
				i++;
				if(line.charAt(i) == 's')
				{
					saveToDisk();
				}
				else{ //it is a 'p'
					requestPrint();
					
					//clear line
					line.delete(0, line.length());
					if(in.hasNextLine())
					{
						line.append(in.nextLine());
						interpretLine();
					}
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
			int sector = dm.getNextFreeSector(diskToUse);

			int i = 0;
			while(line.charAt(i) != 32)
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
			line.append(in.nextLine());

			int writingTo = sector;
			int length = 0;
			while(line.charAt(1) != 'e')
			{
				try {
					disks[diskToUse].write(writingTo, line);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				writingTo++;
				length++;
				
				//clear line
				line.delete(0, line.length());

				//append the next
				line.append(in.nextLine());
			}

			if(line.charAt(1) == 'e')
			{
				line.delete(0, line.length());

			}

			//create a FileInfo object for this file
			FileInfo thisFile = new FileInfo(diskToUse, sector, length);
			StringBuffer n = new StringBuffer(fname);
			dirm.enter(n, thisFile);

			read();
		}

		void requestPrint() {

			int i = 0;
			while(line.charAt(i) != 32)
			{
				i++;
			}

			String fname = getFileName(i+1);
			PrintJobThread p = new PrintJobThread(fname);
			p.start();
		}
	}
	
	class PrintJobThread extends Thread{
		StringBuffer line;
		String name;
	
		PrintJobThread(String s)
		{
			name = s;
			line = new StringBuffer();
		}

		@Override
		public void run(){
			doJob(name);
		}
	
		void doJob(String s)
		{
			StringBuffer file = new StringBuffer(s);
			//check if the file I'm going to print even exists
			FileInfo info = dirm.lookup(file);
			if(info != null)
			{
				int beginSector = info.getSector();
				int disk = info.getDisk();
				int length = info.getLength();
	
				int printerToUse = pm.request();
	
				for(int i = beginSector; i < length; i++)
				{
					try {
						disks[disk].read(i, line);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					printers[printerToUse].print(line);
					line.delete(0, line.length());
				}
			}
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
		
		sectors[sector].append(data);
		Thread.sleep(200);
	}  // call sleep

	StringBuffer read(int sector, StringBuffer data) throws InterruptedException {
		
		data.append(sectors[sector]);
	
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
		b.append('\n');
		CharSequence s = b.subSequence(0, b.length());

			try {
				File file = new File(name);
				if (!file.exists()) {
	     			file.createNewFile();
	  			}

				FileWriter fw = new FileWriter(file, true);
				out = new BufferedWriter(fw);
				out.append(s);
				System.out.println("Success");
				} catch (IOException e) {
					e.printStackTrace();
				} finally
					{ 
					try{
						if(out!=null)
						out.flush();
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
	int getDisk()
	{
		return diskNumber;
	}
	int getSector(){
		return startingSector;
	}
	int getLength(){
		return fileLength;
	}
}


class DirectoryManager {
	private Hashtable<String, FileInfo> T;

	DirectoryManager()
	{
		T = new Hashtable<String, FileInfo>();
	}
	void enter(StringBuffer fileName, FileInfo file){
		T.put(fileName.toString(), file);
	}
	FileInfo lookup(StringBuffer fileName)
	{
		FileInfo data = T.get(fileName.toString());
		if(data != null)
		{
			return data;
		}
		else{
			System.out.println("Could not find the specified file. ");
			return null;
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
			try {
				this.wait();
			} catch (InterruptedException e) {
					// TODO Auto-generated catch block
				e.printStackTrace();
			} // block until someone releases Resource // block until someone releases Resource
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

