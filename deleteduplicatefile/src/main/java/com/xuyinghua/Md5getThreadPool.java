package com.xuyinghua;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.swing.filechooser.FileSystemView;

import org.apache.poi.hssf.record.PageBreakRecord.Break;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class Md5getThreadPool {
	
	public static XSSFWorkbook workbook = null;
	public static ExecutorService threadPool = null;
	public static BufferedWriter wrCheck = null;
	public static BufferedWriter wrDel = null;
	public static String path=null;
	public static File scFile=null;
	
	public static void main(String[] args) {

		Scanner sc = new Scanner(System.in);
		String scPath = null;
		while (true) {
			try {
				// 创建线程池
				threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
				while (true) {
					System.out.println("-----请输入文件地址，多个路径以分号(;)分隔	-----" + "\n-----将在桌面生成校验日志，重复文件将被剪切至文件所在盘根目录-----");
					scPath = sc.nextLine();
					if (scPath.equals("exit")) {
						sc.close();
						System.exit(0);
					} else if (scPath == null || scPath.isEmpty()) {
						System.out.println("-----输入错误，请检查-----");
						continue;
					}
					break;
				}
				long startTime = System.currentTimeMillis();
				initLog();

				for (String string : scPath.split(";")) {
					path = string;
					scFile = new File(path);
					System.out.println("-------开始校验文件夹\"" + scFile.getName() + "\"的内容---------");

					// 执行校验
					getSubFile(scFile);
				}
				// 收尾
				// 关闭线程并设置200ms超时时间，确定是否执行完毕
				threadPool.shutdown();
				while (!threadPool.awaitTermination(2, TimeUnit.SECONDS)) {
				}

				Md5Check.map.clear();
				long endTime = System.currentTimeMillis();
				double cosTime = (endTime - startTime) / 1000.0;

				endLog(cosTime);
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("-----出现异常,请检查输入路径-----");
			}
		}
	}

	/**
	 * 
	 * <p>Title: endLog</p>
	 * <p>Description: 关闭日志</p>
	 * @param cosTime
	 * @throws IOException
	 */
	private static void endLog(double cosTime) throws IOException {
		System.out.println("----执行收尾，写入Excel文件-----");
		wrCheck.newLine();
		wrCheck.write("----全部校验完毕,共耗时："+cosTime+"s----");
		wrCheck.close();
		wrDel.close();
		System.out.println("-------------------------------------");
		System.out.println("全部校验完毕,共耗时："+cosTime+"s\n\n");
		
		
		//excel格式日志
		//自动调节列宽
//		XSSFSheet sheet = workbook.getSheet("sheet1");
//		sheet.autoSizeColumn(0);
//		sheet.autoSizeColumn(1);
//		sheet.autoSizeColumn(2);
//		XSSFSheet sheet2 = workbook.getSheet("sheet2");
//		sheet2.autoSizeColumn(0);
//		sheet2.autoSizeColumn(1);
//		sheet2.autoSizeColumn(2);
//		
		//写入Excel文件，并关闭
//		String excelPath =System.getProperty("user.dir")+"/md5.xlsx";
//		BufferedOutputStream fOut = new BufferedOutputStream(new FileOutputStream(excelPath));
//		workbook.write(fOut);
//		fOut.close();
//		workbook.close();
	}

	/**
	 * 
	 * <p>Title: initLog</p>
	 * <p>Description: 初始化日志</p>
	 * @throws Exception
	 */
	private static void initLog() throws Exception {
		String desktopPath  = FileSystemView.getFileSystemView() .getHomeDirectory().getAbsolutePath();
		wrCheck=new BufferedWriter(new OutputStreamWriter(new FileOutputStream(desktopPath +"/check.txt"), "UTF-8")); 
		wrDel=new BufferedWriter(new OutputStreamWriter(new FileOutputStream(desktopPath +"/delete.txt"), "UTF-8")); 
		wrCheck.write("文件名,md5值,路劲");
		wrCheck.newLine();
		
		wrDel.write("标题,md5值,删除文件,保留文件");
		wrDel.newLine();
		
		
		
		//Excel格式日志，创建excel文档 
//		workbook=new XSSFWorkbook(); 
		//设置标题行 XSSFSheet sheet =
		/*
		 * workbook.createSheet("sheet1"); XSSFRow row = sheet.createRow(0);
		 * row.createCell(0).setCellValue("文件名");
		 * row.createCell(1).setCellValue("md5值");
		 * row.createCell(2).setCellValue("文件路劲");
		 * 
		 * XSSFSheet sheet2 = workbook.createSheet("sheet2"); XSSFRow s2Row =
		 * sheet2.createRow(0); s2Row.createCell(0).setCellValue("文件名");
		 * s2Row.createCell(1).setCellValue("md5值");
		 * s2Row.createCell(2).setCellValue("文件路劲");
		 */

	}

	/**
	 * 
	 * <p>Title: getSubFile</p>
	 * <p>Description: 遍历文件并校验</p>
	 * @param file
	 */
	private static void getSubFile(File file) {
		if (file.isFile()) {
//			threadPool.execute(new Md5Check(file,workbook));
			threadPool.execute(new Md5Check(scFile,file, wrCheck, wrDel));
		} else {
			for (File subFile : file.listFiles()) {
				getSubFile(subFile);
			}
		}
	}
	
}
