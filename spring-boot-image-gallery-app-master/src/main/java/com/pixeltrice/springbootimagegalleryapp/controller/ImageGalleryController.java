package com.pixeltrice.springbootimagegalleryapp.controller;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.sql.Blob;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.pixeltrice.springbootimagegalleryapp.entity.FileDB;
import com.pixeltrice.springbootimagegalleryapp.repository.FileDBRepository;
import com.pixeltrice.springbootimagegalleryapp.service.FileStorageService;
import message.ResponseMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.pixeltrice.springbootimagegalleryapp.entity.ImageGallery;
import com.pixeltrice.springbootimagegalleryapp.service.ImageGalleryService;


@Controller
public class ImageGalleryController {
	
	@Value("${uploadDir}")
	private String uploadFolder;

	@Autowired
	private ImageGalleryService imageGalleryService;

	@Autowired
	private FileStorageService storageService;

	@Autowired
	public FileDBRepository fileDBRepository;

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	@GetMapping(value = {"/", "/home"})
	public String addProductPage() {
		return "index";
	}



	@PostMapping("/upload")
	public void uploadFile(@RequestParam("data") MultipartFile data) throws IOException {
		String message = "";
//		try {
			storageService.store(data);

//			message = "Uploaded the file successfully: " + file.getOriginalFilename();
//			return ResponseEntity.status(HttpStatus.OK).body(new ResponseMessage(message));
//		} catch (Exception e) {
////			message = "Could not upload the file: " + file.getOriginalFilename() + "!";
////			return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(new ResponseMessage(message));
//		}
	}

	@PostMapping("/image/saveImageDetails")
	public @ResponseBody ResponseEntity<?> createProduct(@RequestParam("name") String name,
			@RequestParam("price") double price, @RequestParam("description") String description, Model model, HttpServletRequest request
			,final @RequestParam("image") MultipartFile file) {
		try {
			//String uploadDirectory = System.getProperty("user.dir") + uploadFolder;
			String uploadDirectory = request.getServletContext().getRealPath(uploadFolder);
			log.info("uploadDirectory:: " + uploadDirectory);
			String fileName = file.getOriginalFilename();
			String filePath = Paths.get(uploadDirectory, fileName).toString();
			log.info("FileName: " + file.getOriginalFilename());
			if (fileName == null || fileName.contains("..")) {
				model.addAttribute("invalid", "Sorry! Filename contains invalid path sequence \" + fileName");
				return new ResponseEntity<>("Sorry! Filename contains invalid path sequence " + fileName, HttpStatus.BAD_REQUEST);
			}
			String[] names = name.split(",");
			String[] descriptions = description.split(",");
			Date createDate = new Date();
			log.info("Name: " + names[0]+" "+filePath);
			log.info("description: " + descriptions[0]);
			log.info("price: " + price);
			try {
				File dir = new File(uploadDirectory);
				if (!dir.exists()) {
					log.info("Folder Created");
					dir.mkdirs();
				}
				// Save the file locally
				BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(new File(filePath)));
				stream.write(file.getBytes());
				stream.close();
			} catch (Exception e) {
				log.info("in catch");
				e.printStackTrace();
			}
			storageService.store(file);
//			byte[] imageData = file.getBytes();
//			ImageGallery imageGallery = new ImageGallery();
//			imageGallery.setName(names[0]);
//			imageGallery.setImage(imageData);
//			imageGallery.setPrice(price);
//			imageGallery.setDescription(descriptions[0]);
//			imageGallery.setCreateDate(createDate);
//			imageGalleryService.saveImage(imageGallery);
			log.info("HttpStatus===" + new ResponseEntity<>(HttpStatus.OK));
			return new ResponseEntity<>("Product Saved With File - " + fileName, HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			log.info("Exception: " + e);
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}
	
//	@GetMapping("/image/display/{id}")
//	@ResponseBody
//	void showImage(@PathVariable("id") Long id, HttpServletResponse response, Optional<ImageGallery> imageGallery)
//			throws ServletException, IOException {
//		log.info("Id :: " + id);
//
//
//		imageGallery = imageGalleryService.getImageById(id);
//		response.setContentType("application/json, application/vnd.ms-excel, application/vnd.openxmlformats-officedocument.spreadsheetml.sheet, image/jpeg, image/jpg, image/png, image/gif");
//		response.getOutputStream().write(imageGallery.get().getImage());
//		response.getOutputStream().close();
//
//
//	}
@GetMapping("/image/display/{id}")
@ResponseBody
void showImage(@PathVariable("id") String id, HttpServletResponse response)
		throws ServletException, IOException {
	log.info("Id :: " + id);
	Optional<FileDB> fb = fileDBRepository.findById(id);

	//imageGallery = imageGalleryService.getImageById(id);
	response.setContentType("image/jpeg, image/jpg, image/png, image/gif, application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
	response.getOutputStream().write(fb.get().getData());
	response.getOutputStream().close();


}
//	@GetMapping("/files/{id}")
//	public ResponseEntity<byte[]> getFile(@PathVariable Long id) {
//		//FileDB fileDB = storageService.getFile(id);
//		Optional<ImageGallery> imageGallery = imageGalleryService.getImageById(id);
//		return ResponseEntity.ok()
//				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + imageGallery.get().getDescription() + "\"")
//				.body(imageGallery.get().getImage());
//	}

	@GetMapping("/files/{id}")
	public ResponseEntity<byte[]> getFile(@PathVariable String id) {
		FileDB fileDB = storageService.getFile(id);

		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileDB.getName() + "\"")
				.body(fileDB.getData());
	}

	@GetMapping("/image/imageDetails")
	String showProductDetails(@RequestParam("id") Long id, Optional<ImageGallery> imageGallery, Model model) {
		try {
			log.info("Id :: " + id);
			if (id != 0) {
				imageGallery = imageGalleryService.getImageById(id);
			
				log.info("products :: " + imageGallery);
				if (imageGallery.isPresent()) {
					model.addAttribute("id", imageGallery.get().getId());
					model.addAttribute("description", imageGallery.get().getDescription());
					model.addAttribute("name", imageGallery.get().getName());
					model.addAttribute("price", imageGallery.get().getPrice());
					return "imagedetails";
				}
				return "redirect:/home";
			}
		return "redirect:/home";
		} catch (Exception e) {
			e.printStackTrace();
			return "redirect:/home";
		}	
	}

	@GetMapping("/image/show")
	String show(Model map) {
		//List<ImageGallery> images = imageGalleryService.getAllActiveImages();
		List<FileDB> images = fileDBRepository.findAll();
		map.addAttribute("images", images);
		return "images";
	}
}	

