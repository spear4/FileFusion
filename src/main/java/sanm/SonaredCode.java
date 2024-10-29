package sanm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

public class SonaredCode {
	

	
	static String Filesep  = File.separator;
	
	public void execute(String[] file_paths) throws Exception {

		// tp_code logic--
		String zip_InputPath = file_paths[0];
		String zip_ArchivePath = file_paths[1];
		String output_SOFilePath = file_paths[2];
		String parametric_FilePath = file_paths[3];
		String extract_POSOFilePath = file_paths[4];
		String extract_SOFilePath = file_paths[5];
		String pdf_FilePath = file_paths[6];
		String log_FilePath = file_paths[7];
		String error_FilePath = file_paths[8];
		String file_loop_val = file_paths[9];
		String input_file_name = file_paths[10];
		
		int file_loop = Integer.parseInt(file_loop_val);

		// // --logger start--
		Logger logger = Logger.getLogger(MSI_CTO.class.getName());
		FileHandler log_FileHandler;

		String pattern = "yyyyMMdd";
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
		String date = simpleDateFormat.format(new Date());

		String log_fileName = "log_" + date + ".txt";

		File dir = new File(log_FilePath + Filesep + log_fileName);

		if (dir.exists()) {

			log_FileHandler = new FileHandler(log_FilePath + Filesep + "log_" + date + ".txt", true);
			logger.addHandler(log_FileHandler);
			SimpleFormatter formatter = new SimpleFormatter();
			log_FileHandler.setFormatter(formatter);

		} else {

			log_FileHandler = new FileHandler(log_FilePath + Filesep + "log_" + date + ".txt");
			logger.addHandler(log_FileHandler);
			SimpleFormatter formatter = new SimpleFormatter();
			log_FileHandler.setFormatter(formatter);

		}

		// // --logger end--

		// // Getting zip directory

		File Inputdirectory = new File(zip_InputPath);
		File[] InputFileslist = Inputdirectory.listFiles();

		// // Check whether the directory is empty or not

		if (InputFileslist.length != 0) {

			int del_POSOFiles = 0;			
			
//			System.out.println(file_loop);
//			System.out.println(InputFileslist.length);
//			
			for(int fi = 0; fi < file_loop && fi < InputFileslist.length; fi++) {

				List<String> fileContent = new ArrayList<>();

				// Checking whether the file is normal file i.,e the file is readable

				int del_zips = 0;

				if (InputFileslist[fi].isFile()) {

					// // To check whether there is zip files or not
					
					String zipFile_name = InputFileslist[fi].getName();				

					int input_FileExtensionCount = zipFile_name.lastIndexOf('.');

					String input_FileExtension = zipFile_name.substring(input_FileExtensionCount + 1);

					if (input_FileExtension.equalsIgnoreCase("zip")) {

						// Checking whether the zip is already in the Zip Archive Folder

						// // --start--

						File zip_ArchiveFile = new File(zip_ArchivePath);
						String[] zipArchive_FileList = zip_ArchiveFile.list();

						int zipfile_Count = 0;

						for (int archive_FileCount = 0; archive_FileCount < zipArchive_FileList.length; archive_FileCount++) {

							String archived_file_Name = zipArchive_FileList[archive_FileCount];

							if (archived_file_Name.equalsIgnoreCase(zipFile_name)) {

								zipfile_Count = 1;

							}

						}

						// // --end--

						if (zipfile_Count != 1) {

							// int to check if the zip contains PO_SO tab file and SO XMl
							// file
							int po_so = 0;
							int so = 0;

							// for archive check condition
							int can_archive_zip = 0;

							// // Zip Extraction
							// // --Start--

							// // Notes :
							// // InputFileslist[fi] variable contains zip files with path
							// // zipFile_name variable contains zip files only
							
							File out_path = new File(parametric_FilePath);
							FileInputStream zip_fis;
							byte[] zip_buffer = new byte[1024];
							zip_fis = new FileInputStream(InputFileslist[fi]);
							ZipInputStream zis = new ZipInputStream(zip_fis);
							ZipEntry ze = zis.getNextEntry();

							while (ze != null) {

								String zip_files = ze.getName();

								fileContent.add(zip_files);

								// // PO | SO Text File Moving
								// --start--
								File extract_POSOFileDirectory = new File(extract_POSOFilePath);

								if (zip_files.contains(input_file_name + "-B2B_SFTP_PO")) {

									// System.out.println("Zip Files PO|SO
									// :"
									// + zip_files);

									try {

										File POSO_FilePath = new File(
												extract_POSOFileDirectory + Filesep + zip_files);
										new File(POSO_FilePath.getParent()).mkdirs();
										FileOutputStream POSO_fileExtractToDirectory = new FileOutputStream(
												POSO_FilePath);
										int POSO_FileCount;
										while ((POSO_FileCount = zis.read(zip_buffer)) > 0) {
											POSO_fileExtractToDirectory.write(zip_buffer, 0, POSO_FileCount);
										}
										POSO_fileExtractToDirectory.close();

									} catch (Exception POSO_TextFileMovingError) {

										// TODO: handle exception
										logger.info("ERROR :  Occured While moving PO | SO Text File taken from : "
												+ zip_InputPath + "  is " + POSO_TextFileMovingError);

										can_archive_zip = 2;

									}

									po_so = 1;

								}

								// --end--

								// // SO XML File Processing
								// --start--

								File SO_fileExtractToPath = new File(extract_SOFilePath);
								File output_SOpath = new File(output_SOFilePath);

								// For output xml file naming convention
								// --start--

								String output_name = InputFileslist[fi].getName();
								int output_ext = output_name.lastIndexOf('.');
								String output_SOfileName = output_name.substring(0, output_ext);

								// --end--

								if (zip_files.contains(input_file_name + "-B2B_SFTP_SO")) {

									// System.out.println("Zip Files SO :" + zip_files);

									// // SO File moving to
									// extracted\\xml_files

									// --start--

									File SO_fileExtractDirectory = new File(
											SO_fileExtractToPath + Filesep + zip_files);

									try {
										new File(SO_fileExtractDirectory.getParent()).mkdirs();
										FileOutputStream SO_fos = new FileOutputStream(SO_fileExtractDirectory);

										int len2;

										while ((len2 = zis.read(zip_buffer)) > 0) {
											SO_fos.write(zip_buffer, 0, len2);
										}
										SO_fos.close();

									} catch (Exception SO_xml_fileMovingError) {

										// TODO: handle exception

										logger.info("ERROR : Occured While moving SO XML File taken from : "
												+ zip_InputPath + "  is " + SO_xml_fileMovingError);

										can_archive_zip = 2;

									}

									// --end--

									// // Getting values from PO text File
									// // --start--

									File extTextFilePath = new File(extract_POSOFilePath);
									File[] extTextFileList = extTextFilePath.listFiles();

									String po_type = "";
									String po_shipToSite = "";
									String po_supplierId = "";
									String po_supplierSite = "";
									String po_legalEntity = "";
									String po_shipMethod = "";
									String po_num = "";
									String poState = "";
									String revNumber = "";
									String po_priority = "";

									for (File extText_Files : extTextFileList) {

										BufferedReader POSO_br = new BufferedReader(new FileReader(extText_Files));

										// // Line By line reading
										ArrayList<String> lines = new ArrayList<>();
										String line;

										try {

											while ((line = POSO_br.readLine()) != null) {
												
												lines.add(line);
												
											}

											String po_line = lines.get(1);

											po_type = po_line.split("\t")[69];
											po_shipToSite = po_line.split("\t")[137];
											po_supplierId = po_line.split("\t")[20];
											po_supplierSite = po_line.split("\t")[183];
											po_legalEntity = po_line.split("\t")[71];
											po_shipMethod = po_line.split("\t")[114];
											po_num = po_line.split("\t")[0];
											poState = po_line.split("\t")[2];
											revNumber = po_line.split("\t")[219];
											po_priority = po_line.split("\t")[78];

										} catch (Exception posoValError) {

											// TODO: handle exception
											System.out.println(
													"ERROR : Occured while getting values from POSO tab-delimiter file : "
															+ posoValError);
											can_archive_zip = 2;

										}

										POSO_br.close();

									}

									// // --end--

									// // Inserting processed values into
									// XML Files
									// // --start--

									if (po_so == 0) {

										can_archive_zip = 2;

									} else {

										try {

											DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory
													.newInstance();
											DocumentBuilder documentBuilder = documentBuilderFactory
													.newDocumentBuilder();
											Document document = documentBuilder.parse(SO_fileExtractDirectory);
											document.getDocumentElement().normalize();

											// Implement Elements for new
											// MSI CTO file
											// format(28.02.2023)
											Element row = (Element) document.getElementsByTagName("HDR_WAREHOUSE_CODE")
													.item(0);
											Element refChild = (Element) document
													.getElementsByTagName("CUSTOMER_SALES_ORDER_NUMBER").item(0);

											Element newElement = document.createElement("PONUM");
											newElement.setTextContent(po_num);
											Element newElement1 = document.createElement("PO_TYPE");
											newElement1.setTextContent(po_type);

											Element newElement2 = document.createElement("SHIP_TO_SITE");
											newElement2.setTextContent(po_shipToSite);

											Element newElement3 = document.createElement("SUPPLIER_ID");
											newElement3.setTextContent(po_supplierId);

											Element newElement4 = document.createElement("SUPPLIER_SITE");
											newElement4.setTextContent(po_supplierSite);

											Element newElement5 = document.createElement("LEGAL_ENTITY");
											newElement5.setTextContent(po_legalEntity);

											Element newElement6 = document.createElement("SHIP_METHOD");
											newElement6.setTextContent(po_shipMethod);

											Element newElement7 = document.createElement("PO_STATE");
											newElement7.setTextContent(poState);

											Element newElement8 = document.createElement("REV_NUMBER");
											newElement8.setTextContent(revNumber);

											Element newElement9 = document.createElement("PO_PRIORITY");
											newElement9.setTextContent(po_priority);

											Text lineBreak = document.createTextNode("\n");
											Text lineBreak1 = document.createTextNode("\n");
											Text lineBreak2 = document.createTextNode("\n");
											Text lineBreak3 = document.createTextNode("\n");
											Text lineBreak4 = document.createTextNode("\n");
											Text lineBreak5 = document.createTextNode("\n");
											Text lineBreak6 = document.createTextNode("\n");
											Text lineBreak7 = document.createTextNode("\n");
											Text lineBreak8 = document.createTextNode("\n");
											Text lineBreak9 = document.createTextNode("\n");

											Text tabBreak = document.createTextNode("\t");
											Text tabBreak1 = document.createTextNode("\t");
											Text tabBreak2 = document.createTextNode("\t");
											Text tabBreak3 = document.createTextNode("\t");
											Text tabBreak4 = document.createTextNode("\t");
											Text tabBreak5 = document.createTextNode("\t");
											Text tabBreak6 = document.createTextNode("\t");
											Text tabBreak7 = document.createTextNode("\t");
											Text tabBreak8 = document.createTextNode("\t");

											row.getParentNode().insertBefore(newElement, refChild);
											row.getParentNode().insertBefore(newElement.appendChild(lineBreak),
													refChild);

											row.getParentNode().insertBefore(newElement1.appendChild(tabBreak),
													refChild);
											row.getParentNode().insertBefore(newElement1, refChild);
											row.getParentNode().insertBefore(newElement1.appendChild(lineBreak1),
													refChild);

											row.getParentNode().insertBefore(newElement2.appendChild(tabBreak1),
													refChild);
											row.getParentNode().insertBefore(newElement2, refChild);
											row.getParentNode().insertBefore(newElement2.appendChild(lineBreak2),
													refChild);

											row.getParentNode().insertBefore(newElement3.appendChild(tabBreak2),
													refChild);
											row.getParentNode().insertBefore(newElement3, refChild);
											row.getParentNode().insertBefore(newElement3.appendChild(lineBreak3),
													refChild);

											row.getParentNode().insertBefore(newElement4.appendChild(tabBreak3),
													refChild);
											row.getParentNode().insertBefore(newElement4, refChild);
											row.getParentNode().insertBefore(newElement4.appendChild(lineBreak4),
													refChild);

											row.getParentNode().insertBefore(newElement5.appendChild(tabBreak4),
													refChild);
											row.getParentNode().insertBefore(newElement5, refChild);
											row.getParentNode().insertBefore(newElement5.appendChild(lineBreak5),
													refChild);

											row.getParentNode().insertBefore(newElement6.appendChild(tabBreak5),
													refChild);
											row.getParentNode().insertBefore(newElement6, refChild);
											row.getParentNode().insertBefore(newElement6.appendChild(lineBreak6),
													refChild);

											// New Element as per Update

											row.getParentNode().insertBefore(newElement7.appendChild(tabBreak6),
													refChild);
											row.getParentNode().insertBefore(newElement7, refChild);
											row.getParentNode().insertBefore(newElement7.appendChild(lineBreak7),
													refChild);

											row.getParentNode().insertBefore(newElement8.appendChild(tabBreak7),
													refChild);
											row.getParentNode().insertBefore(newElement8, refChild);
											row.getParentNode().insertBefore(newElement8.appendChild(lineBreak8),
													refChild);

											// New po_priority element added on 6/5/2023

											row.getParentNode().insertBefore(newElement9.appendChild(tabBreak8),
													refChild);
											row.getParentNode().insertBefore(newElement9, refChild);
											row.getParentNode().insertBefore(newElement9.appendChild(lineBreak9),
													refChild);

											DOMSource source = new DOMSource(document);

											TransformerFactory transformerFactory = TransformerFactory.newInstance();
											Transformer transformer = transformerFactory.newTransformer();
											transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
											StreamResult result = new StreamResult(
													output_SOpath + Filesep + output_SOfileName + "_Output.xml");
											transformer.transform(source, result);

											logger.info("Output File - " + output_SOfileName
													+ ".xml file which is generated from " + zip_InputPath
													+ " processed successfully and archived in : " + output_SOpath);

											can_archive_zip = 1;

										} catch (Exception xmlOutError) {

											// TODO: handle exception

											logger.warning("ERROR : Occured while processing the " + output_SOfileName
													+ " ouput XML File which is generated from " + zip_InputPath
													+ " is - " + xmlOutError);

											can_archive_zip = 2;

										}

									}

									// // --end of XML Output File Processing--

									so = 1;

								}

								// --end of SO File Processing--

								// // close this ZipEntry
								zis.closeEntry();
								ze = zis.getNextEntry();

							}

							// Check if zip file contains PO_SO and SO files
							// --start--

							if (po_so == 0) {

								can_archive_zip = 2;
								logger.info("No PO_SO Tab Delimiter File Found inside the Zip file : " + zipFile_name
										+ "Which is taken from" + zip_InputPath);
								
							}
							if (so == 0) {
								
								can_archive_zip = 2;
								logger.info("No SO XML File Found inside the Zip file : " + zipFile_name
										+ "Which is taken from" + zip_InputPath);
								
							}

							// --End of POSO and SO Checking--

							// Archive Parametric and PDF Files
							// -- Start --

							if (can_archive_zip == 1) {

								int forPDF = 0;
								int forPara = 0;

								// Getting Parametric and PDF Files and moving to their
								// respective archive folders
								// -- Strat --
								
								zip_fis = new FileInputStream(InputFileslist[fi]);
								ZipInputStream zis1 = new ZipInputStream(zip_fis);
								ZipEntry ze1 = zis1.getNextEntry();

								while(ze1!=null){

									// // Copying Parametric File from Zips to
									// Parametric Folder
									// --start--
									String file_name=ze1.getName();
//									System.out.println(file_name);System.exit(0);
									if (file_name.contains("Parametric")) {
										try {

											File para_File = new File(out_path + Filesep + file_name);

											// create directories for sub
											// directories in zip

											new File(para_File.getParent()).mkdirs();
											FileOutputStream fos = new FileOutputStream(para_File);

											int len;
											while ((len = zis1.read(zip_buffer)) > 0) {

												fos.write(zip_buffer, 0, len);

											}

											fos.close();

											logger.info("Parametric File : " + file_name
													+ " file is archived successfully in " + out_path);

										} catch (Exception para_movingError) {

											// TODO: handle exception
											logger.info("Error occured while archiving Parametric File - " + file_name
													+ " file : " + para_movingError);

										}

										forPara = 1;

									}

									// --end of copying Parametric File--

									// Copying PDF File from Zips to PDF Folder
									// --start--

									int ext1 = file_name.lastIndexOf('.');
									String extension1 = file_name.substring(ext1 + 1);
									File pdf_OutPath = new File(pdf_FilePath);

									if (extension1.equals("pdf")) {

										try {

											File pdf_OutFilePath = new File(pdf_OutPath + Filesep + file_name);
											new File(pdf_OutFilePath.getParent()).mkdirs();
											FileOutputStream fos3 = new FileOutputStream(pdf_OutFilePath);
											int len3;
											while ((len3 = zis1.read(zip_buffer)) > 0) {
												fos3.write(zip_buffer, 0, len3);
											}

											fos3.close();

											logger.info("PDF File : " + file_name + " file is archived successfully in "
													+ pdf_OutPath);

										} catch (Exception pdf_moveError) {

											// TODO: handle exception
											logger.info("ERROR : Occured while archiving PDF File :  " + file_name
													+ " file : " + pdf_moveError);

										}

										forPDF = 1;

									}

									// --end of copying PDF File--
									zis1.closeEntry();
									ze1 = zis1.getNextEntry();
								}

								// -- END of Getting Para/PDF Files from the ZIP --

								// Checking if the zip file from the input folder
								// contains parametric and pdf file
								// -- Start --

								if (forPDF == 0) {
									logger.info("No PDF File found inside " + zipFile_name + " Zip File taken from "
											+ zip_InputPath);
								}
								if (forPara == 0) {
									logger.info("No Parametric File found inside " + zipFile_name
											+ " Zip File taken from " + zip_InputPath);
								}

								// -- End of PARA/PDF files checking --

								// -- End of PARAMETRIC | PDF File Archiving --

								// Archive Zip FIles that are processed successfully

								// // Copying the new Zip Files to the Input Archive
								// // --start--

								InputStream input_zipPath = null;
								OutputStream output_ZipArchive = null;

								try {

									input_zipPath = new FileInputStream(InputFileslist[fi]);
									output_ZipArchive = new FileOutputStream(
											zip_ArchivePath + Filesep + zipFile_name);
									byte[] zipArchiveBuffer = new byte[1024];
									int bytesRead;
									while ((bytesRead = input_zipPath.read(zipArchiveBuffer)) > 0) {
										output_ZipArchive.write(zipArchiveBuffer, 0, bytesRead);
									}

									input_zipPath.close();
									output_ZipArchive.close();

									logger.info("Good zip file : " + zipFile_name + " is archived in " + zip_ArchivePath
											+ " Successfully");

									del_zips = 1;

								} catch (Exception goodZipArchive_Error) {

									// System.out.println(zipCopy_Error);
									logger.info("ERROR : Occured while archiving good zip file" + zipFile_name
											+ " which is taken from " + zip_InputPath + "is - " + goodZipArchive_Error);

									can_archive_zip = 2;

									del_zips = 1;

								}

								// // --end--

							} else if (can_archive_zip == 2) {

								InputStream input_errorZip = null;
								OutputStream output_errorZip = null;

								try {

									input_errorZip = new FileInputStream(InputFileslist[fi]);
									output_errorZip = new FileOutputStream(
											error_FilePath + Filesep + zipFile_name);
									byte[] error_zipArchiveBuffer = new byte[1024];
									int error_byteRead;

									while ((error_byteRead = input_errorZip.read(error_zipArchiveBuffer)) > 0) {

										output_errorZip.write(error_zipArchiveBuffer, 0, error_byteRead);

									}
									input_errorZip.close();
									output_errorZip.close();

									// System.out.println(
									// "Zip Files are archived successfully");

									logger.info("ERROR : Bad zip file - " + zipFile_name + " is archived in "
											+ error_FilePath + " Successfully");

									del_zips = 1;

								} catch (Exception badZipArchiveError) {

									// TODO: handle exception

									logger.warning(
											"Error occured while archiving the bad zip file : " + badZipArchiveError);

									del_zips = 1;

								}

							}

							// --Archiving Zip Files end--

							// close last ZipEntry

							zis.closeEntry();
							zis.close();
							zip_fis.close();

							del_POSOFiles = 1;

						} else {

							logger.info("ERROR : " + zipFile_name + " Zip File taken from " + zip_InputPath
									+ " is already Archived & Output already processed");

						}

						// // --end--

					} else {

						logger.info(zipFile_name + " is not a zip file which is in " + zip_InputPath + " folder ");

					}

				} else {

					// System.out.println("File Corrupted");
					logger.info("Some error while processing the " + InputFileslist[fi].getName() + " file taken from "
							+ zip_InputPath);

				}

				if (del_zips == 1) {

					try {

						InputFileslist[fi].delete();
						logger.info("Proccessed zipfile : " + InputFileslist[fi].getName() + " is deleted successfully from the input folder :" + zip_InputPath);

					} catch (Exception processedZipDeleteError) {

						// TODO: handle exception
						logger.info("ERROR : Occured while deleting the zip files  : " + processedZipDeleteError
								+ " from the input folder " + zip_InputPath);
					}

				}

			}

			// --end--

			// Deleting the POSO Files after Processing the output
			// --start--

			if (del_POSOFiles == 1) {

				File POSO_Path = new File(extract_POSOFilePath);

				for (File POSO_Files : POSO_Path.listFiles()) {

					String poso_fileName = POSO_Files.getName();

					try {

						POSO_Files.delete();

						System.out.println("INFO : PO_SO Tab Delimiter file - " + poso_fileName + " from the path - "
								+ POSO_Path + " is deleted successfully");

					} catch (Exception POSO_fileDeletionError) {

						// TODO: handle exception
						logger.info("Occured context while deleting the POSO Files : " + POSO_fileDeletionError
								+ " from the " + POSO_Path + " directory");
					}

				}

				File SO_Path = new File(extract_SOFilePath);

				for (File SO_Files : SO_Path.listFiles()) {

					String SO_fileName = SO_Files.getName();

					try {

						SO_Files.delete();
						System.out.println("INFO : SO XML file - " + SO_fileName + " from the path - " + SO_Path
								+ " is deleted successfully");

					} catch (Exception SO_fileDeletionError) {

						// TODO: handle exception
						logger.info("Occured context while deleting the SO files : " + SO_fileDeletionError
								+ " from the " + SO_Path + " directory");

					}

				}

			}

			// --end--

		} else {

			logger.info(zip_InputPath + " folder is empty");

		}

		logger.info("------------------------------------------");

		log_FileHandler.close();

	}

	
	public static void main(String[] args) throws IOException {

		SonaredCode msi_cto = new SonaredCode();

		Path currentDirectoryPath = FileSystems.getDefault().getPath("");

//		System.out.println("File Path :" + currentDirectoryPath);

		String currentDirectoryName = currentDirectoryPath.toAbsolutePath().toString();

		// System.out.println("currentDirectoryPath : " + currentDirectoryName);

		File property_path = new File(currentDirectoryName);

		for (File property_files : property_path.listFiles()) {

			// System.out.println(pro_file.getName().endsWith("properties"));

			if (property_files.getName().endsWith("properties")) {

				String property_fileList = currentDirectoryName +  Filesep + property_files.getName();
				
//				System.out.println("Files :" + property_fileList);

				FileReader reader = new FileReader(property_fileList);

				Properties p = new Properties();
				p.load(reader);

				if (p.getProperty("SUPPLIER_ID") != null) {

					String supplier_IdList = p.getProperty("SUPPLIER_ID");

					// System.out.println(supplier_IdList);

					if (supplier_IdList.isEmpty()) {

						System.out.println("INFO : Supplier ID's not present in the " + property_files.getName() + " file");

					} else {

						for (String supplier_Id : supplier_IdList.split(",")) {

							// System.out.println(supplier_Id);

							String supplier_IdPaths = p.getProperty(supplier_Id);

							try {

								msi_cto.execute(supplier_IdPaths.split(","));

							} catch (Exception e) {

								// TODO Auto-generated catch block
								e.printStackTrace();
								System.out.println(
										"Error Occured while pushing supplier id paths for zip file processing");

							}

						}

					}

				} else {

					System.out.println(
							"INFO : No key is present in the name of 'SUPPLIER_ID' inside the property file taken from - "
									+ property_fileList);

				}

			} else {

				System.out.println("INFO : " + property_files.getName()
						+ " is not a properties file that found in the directory - " + currentDirectoryName);

			}

			// System.out.println(p.getProperty("password"));

		}

	}


}
