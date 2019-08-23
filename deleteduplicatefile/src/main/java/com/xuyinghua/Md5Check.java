package com.xuyinghua;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.zip.CRC32;

import org.apache.commons.codec.binary.Hex;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class Md5Check implements Runnable{
	
		
	private File file;
	private XSSFWorkbook workbook;
	private BufferedWriter wrCheck;
	private BufferedWriter wrDel;
	public static HashMap<String, String> map=new HashMap<String, String>();
	
	public Md5Check(File file, XSSFWorkbook workbook) {
		super();
		this.file = file;
		this.workbook = workbook;
	}
	public Md5Check(File file, BufferedWriter wrCheck,BufferedWriter wrDel) {
		super();
		this.file = file;
		this.wrCheck = wrCheck;
		this.wrDel = wrDel;
	}
	
	/**
	 * 
	 */
	public void run() {
		try {
			System.out.println("校验\t"+"线程名："+Thread.currentThread().getName()+"\t文件名: "+file.getPath());
			String md5 = getMd5ByFile(file);
			//String md5 = getCrcByFile(file);
//			System.out.println("校验成功--"+file.getName()+"\t MD5:"+md5+"\t线程名："+Thread.currentThread().getName());
			
			//记录操作并判断删除重复文件
			synchronized(Md5Check.class){
				endCheck(md5);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("异常文件为："+file.getAbsolutePath());
		}
	}
	
	/**
	 * crc32更快,碰撞率更高，来自https://blsog.csdn.net/yunhua_lee/article/details/42775039中的数据，
	 * 1820w数据样本中，出现3.8w的重复
	 */
	public String getCrcByFile(File file) throws Exception {
		BufferedInputStream inputStream=null;
		try {
			/*BufferedInputStream的默认缓冲区大小是8192字节。
			当FileInputStream每次读取数据量接近或远超这个值时，两者效率就没有明显差别了。
			劳资错觉，网上有个测速就是BufferedInputStream觉得快一丢丢，可能是正常误差*/
			
			inputStream = new BufferedInputStream(new FileInputStream(file));
			//FileInputStream inputStream =new FileInputStream(file);
	        CRC32 crc = new CRC32();
	        byte[] bytes = new byte[8192];
	        int cnt;
	        while ((cnt = inputStream.read(bytes)) != -1) {
	            crc.update(bytes, 0, cnt);
	        }
	        inputStream.close();
	        return Long.toHexString(crc.getValue()).toUpperCase();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}finally {
			if (inputStream !=null) inputStream.close();
		}
	}
	
	/**
	 * @see 计算md5方法
	 * @param file
	 * @return
	 * @throws Exception
	 */
	public String getMd5ByFile(File file) throws Exception {
		 FileInputStream fileInputStream = null;
	       try {
	         MessageDigest MD5 = MessageDigest.getInstance("MD5");
	          fileInputStream = new FileInputStream(file);
	          byte[] buffer = new byte[256*1024];
	          if (file.length()>512*1024) {
	        	  buffer = new byte[10*1024*1024];
	          }
	          
	          int length;
	           while ((length = fileInputStream.read(buffer)) != -1) {
	              MD5.update(buffer, 0, length);
	         }
	           return new String(Hex.encodeHex(MD5.digest()));
	     } catch (Exception e) {
	           e.printStackTrace();
	            return null;
	        } finally {
	           try {
	             if (fileInputStream != null){
	                   fileInputStream.close();
	                    }
	           } catch (IOException e) {
	             e.printStackTrace();
	            }
	       }
	  }

	private void endCheck(String md5) throws Exception {
		/**
		 * 记录校验文件
		 */
		/*XSSFSheet sheet1 = workbook.getSheet("sheet1");
		XSSFRow row = sheet1.createRow(sheet1.getLastRowNum()+1);
		row.createCell(0).setCellValue(file.getName());
		row.createCell(1).setCellValue(md5);
		
		XSSFCell cell2 = row.createCell(2);
		cell2.setCellValue(file.getAbsolutePath());

		XSSFHyperlink hyperlink = workbook.getCreationHelper().createHyperlink(Hyperlink.LINK_FILE);
		hyperlink.setAddress(file.toURI().toString());
		cell2.setHyperlink(hyperlink);*/
		
		wrCheck.write(file.getName()+",\t"+md5+",\t"+file.getAbsolutePath());
		wrCheck.newLine();
		
		/**
		 * 判断删除重复文件并记录
		 */
		if(!map.containsKey(md5)){
			map.put(md5, file.getAbsolutePath());
		}else {
			//留下绝对路径名短的
			File sFile = new File(map.get(md5));
			int sl = sFile.getAbsolutePath().length();
			int l=file.getAbsolutePath().length();
			File dFile=file;
			if(sl>l) {
				map.put(md5, file.getAbsolutePath());
				dFile=sFile;
			}
			File deFile=new File(
					dFile.getAbsolutePath().replace(
							new File(Md5getThreadPool.path).getAbsolutePath(), 
							dFile.getAbsolutePath().charAt(0)+":/delete"));
			
			if(!deFile.getParentFile().exists()) 
				deFile.getParentFile().mkdirs();
			dFile.renameTo(deFile);
			System.out.println("重复文件删除："+dFile.getAbsolutePath());
			//记录删除文件
			/*XSSFSheet sheet2 = workbook.getSheet("sheet2");
			row = sheet2.createRow(sheet2.getLastRowNum()+1);
			
			row.createCell(0).setCellValue(dFile.getName());
			row.createCell(1).setCellValue(md5);
			row.createCell(2).setCellValue(dFile.getAbsolutePath());*/
			wrDel.write("重复文件删除---,\t"+md5+",\t"+dFile.getAbsolutePath()+",\t"+map.get(md5));
			wrDel.newLine();
		}
	}
	
}
