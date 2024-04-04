/*
package com.asrevo.cvhome.store.controller.v1.content;

import com.asrevo.cvhome.store.controller.exception.RestApiException;
import com.asrevo.cvhome.store.controller.exception.ServiceRuntimeException;
import com.asrevo.cvhome.store.core.entity.content.FileContentType;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.core.model.content.ContentFile;
import com.asrevo.cvhome.store.core.model.content.ContentFolder;
import com.asrevo.cvhome.store.service.facade.content.ContentFacade;
import com.asrevo.cvhome.store.utils.FileNameUtils;
import com.asrevo.cvhome.store.utils.ImageFilePath;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.asrevo.cvhome.store.core.model.content.Content;
*/
/**
 * Administration tool dedicated api
 *
 * @author carlsamson
 * <p>
 * <p>
 * Works with ng-file-man client
 * @param path
 * @param merchantStore
 * @param language
 * @return
 * @throws Exception
 * @param path
 * @param merchantStore
 * @param language
 * @return
 * @throws Exception
 * <p>
 * works with file manager (javascript client)
 * @param files
 * @param merchantStore
 * @param language
 * <p>
 * Works with ng-file-man client
 * @param path
 * @param merchantStore
 * @param language
 * @return
 * @throws Exception
 * @param path
 * @param merchantStore
 * @param language
 * @return
 * @throws Exception
 * <p>
 * works with file manager (javascript client)
 * @param files
 * @param merchantStore
 * @param language
 * <p>
 * Works with ng-file-man client
 * @param path
 * @param merchantStore
 * @param language
 * @return
 * @throws Exception
 * @param path
 * @param merchantStore
 * @param language
 * @return
 * @throws Exception
 * <p>
 * works with file manager (javascript client)
 * @param files
 * @param merchantStore
 * @param language
 * <p>
 * Works with ng-file-man client
 * @param path
 * @param merchantStore
 * @param language
 * @return
 * @throws Exception
 * @param path
 * @param merchantStore
 * @param language
 * @return
 * @throws Exception
 * <p>
 * works with file manager (javascript client)
 * @param files
 * @param merchantStore
 * @param language
 *//*

@RestController
@RequestMapping(value = "/api/v1")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ContentAdministrationApi {
	
	
	private static final String DEFAULT_PATH = "/";
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ContentAdministrationApi.class);

	@Autowired
	private ContentFacade contentFacade;
	
	@Autowired
	private FileNameUtils fileNameUtils;
	
	
	@Autowired
	@Qualifier("img")
	private ImageFilePath imageUtils;
	
	*/
/**
 * Works with ng-file-man client
 *
 * @param path
 * @param merchantStore
 * @param language
 * @return
 * @throws Exception
 *//*

	@GetMapping(value = "/private/content/list", produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiImplicitParams({ @ApiImplicitParam(name = "store", dataType = "String", defaultValue = "DEFAULT"),
			@ApiImplicitParam(name = "lang", dataType = "String", defaultValue = "en") })
	public List<ImageFile> list(@RequestParam(value = "parentPath", required = false) String path,
			@ApiIgnore MerchantStore merchantStore, @ApiIgnore Language language) throws Exception {

		String decodedPath = decodeContentPath(path);

		ContentFolder folder = contentFacade.getContentFolder(decodedPath, merchantStore);
		List<ImageFile> files = folder.getContent().stream().map(x -> convertToImageFile(merchantStore, x))
				.collect(Collectors.toList());

		return files;
	}
	
	*/
/**
 * @param path
 * @param merchantStore
 * @param language
 * @return
 * @throws Exception
 *//*

	@GetMapping(value = "/private/content/folder", produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiImplicitParams({ @ApiImplicitParam(name = "store", dataType = "String", defaultValue = "DEFAULT"),
			@ApiImplicitParam(name = "lang", dataType = "String", defaultValue = "en") })
	public ContentFolder folder(
			@RequestParam(value = "path", required = false) String path,
			@ApiIgnore MerchantStore merchantStore,
			@ApiIgnore Language language) throws Exception {
		String decodedPath = decodeContentPath(path);
		return contentFacade.getContentFolder(decodedPath, merchantStore);
	}
	
	
	*/
/**
 * works with file manager (javascript client)
 * @param files
 * @param merchantStore
 * @param language
 *//*

	@PostMapping(value = "/private/content/images/add", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
	@ResponseStatus(HttpStatus.CREATED)
	@ApiImplicitParams({
			@ApiImplicitParam(name = "store", dataType = "String", defaultValue = "DEFAULT"),
			@ApiImplicitParam(name = "lang", dataType = "String", defaultValue = "en") })
	public FileStatus upload(
			@RequestParam(value = "qqfile", required = true) MultipartFile qqfile,
			@RequestParam(value = "qquuid", required = true) String qquuid,
			@RequestParam(value = "qqfilename", required = true) String qqfilename,
			@RequestParam(value = "qqtotalfilesize", required = false) Long qqtotalfilesize,
			@RequestParam(value = "parentPath", required = false) String parentPath,
			@RequestParam(value = "qqpartindex", required = false) Integer qqpartindex,
			@RequestParam(value = "qqtotalparts", required = false) Integer qqtotalparts,
			@ApiIgnore MerchantStore merchantStore, 
			@ApiIgnore Language language) {
		
		    if(!fileNameUtils.validFileName(qqfilename)) {
				FileStatus fs = new FileStatus();
				fs.setError("Invalid filename");
				fs.setSuccess(false);
				return fs;
		    }

			ContentFile cf = new ContentFile();
			cf.setContentType(qqfile.getContentType());
			cf.setName(qqfilename);
			try {
				cf.setFile(qqfile.getBytes());
				contentFacade.addContentFile(cf, merchantStore.getCode());
				return new FileStatus();
			} catch (IOException e) {
				//throw new ServiceRuntimeException("Error while getting file bytes");
				LOGGER.error("Error when uploadging file",e);
				FileStatus fs = new FileStatus();
				fs.setError(e.getMessage());
				fs.setSuccess(false);
				return fs;
			}

	}
	
	@GetMapping(value = "/content/images/download")
	@ApiImplicitParams({ @ApiImplicitParam(name = "store", dataType = "String", defaultValue = "DEFAULT"),
			@ApiImplicitParam(name = "lang", dataType = "String", defaultValue = "en") })
	public @ResponseBody String download(
			@RequestParam(value = "path", required = true) String path,
			@ApiIgnore MerchantStore merchantStore, 
			@ApiIgnore Language language) {
		String fileName = path.substring(path.lastIndexOf("/")+1, path.length());
		try {
	    
			//OutputContentFile file = contentFacade.download(merchantStore, FileContentType.IMAGE, fileName);
			//return file.getFile().toByteArray();
			//return "https://s3.ca-central-1.amazonaws.com/shopizer-carl/files/DEFAULT/85.jpg";
			return null;
		} catch (Exception e) {
			//throw new ServiceRuntimeException("Error while getting file bytes");
			LOGGER.error("Error when renaming file",e);
			throw new ServiceRuntimeException("Error while downloading file [" + fileName + "]");
		}

	}
	
	@PostMapping(value = "/private/content/images/rename", produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.OK)
	@ApiImplicitParams({
			@ApiImplicitParam(name = "store", dataType = "String", defaultValue = "DEFAULT"),
			@ApiImplicitParam(name = "lang", dataType = "String", defaultValue = "en") })
	public FileStatus rename(
			@RequestParam(value = "path", required = true) String path,
			@RequestParam(value = "newName", required = true) String newName,
			@ApiIgnore MerchantStore merchantStore, 
			@ApiIgnore Language language) {

		try {
			
			String fileName = path.substring(path.lastIndexOf("/")+1, path.length());
			contentFacade.renameFile(merchantStore, FileContentType.IMAGE, fileName, newName);
			return new FileStatus();
		} catch (Exception e) {
			//throw new ServiceRuntimeException("Error while getting file bytes");
			LOGGER.error("Error when renaming file",e);
			FileStatus fs = new FileStatus();
			fs.setError(e.getMessage());
			fs.setSuccess(false);
			return fs;
		}

	}
	
	@DeleteMapping(value = "/private/content/images/remove", produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.OK)
	@ApiImplicitParams({
			@ApiImplicitParam(name = "store", dataType = "String", defaultValue = "DEFAULT"),
			@ApiImplicitParam(name = "lang", dataType = "String", defaultValue = "en") })
	public FileStatus remove(
			@RequestParam(value = "path", required = true) String path,
			@ApiIgnore MerchantStore merchantStore, 
			@ApiIgnore Language language) {

		try {
			
			String fileName = path.substring(path.lastIndexOf("/")+1, path.length());
			contentFacade.delete(merchantStore, fileName, FileContentType.IMAGE.name());
			return new FileStatus();
		} catch (Exception e) {
			//throw new ServiceRuntimeException("Error while getting file bytes");
			LOGGER.error("Error when renaming file",e);
			FileStatus fs = new FileStatus();
			fs.setError(e.getMessage());
			fs.setSuccess(false);
			return fs;
		}

	}


	private ImageFile convertToImageFile(MerchantStore store, Content content) {
		ImageFile f = new ImageFile();
		f.setDir(false);
		f.setId(imageUtils.buildStaticImageUtils(store, content.getName()));
		f.setName(content.getName());
		f.setUrl(imageUtils.buildStaticImageUtils(store, content.getName()));
		f.setPath("image.png");
		f.setSize(null);
		return f;
	}

	private ImageFile convertToFolder(String folder) {
		ImageFile f = new ImageFile();
		f.setDir(true);
		f.setId(UUID.randomUUID().toString());
		f.setName(DEFAULT_PATH + "images");
		f.setUrl(DEFAULT_PATH + "images");
		f.setPath(DEFAULT_PATH + "images");
		return f;
	}
	
	private String decodeContentPath(String path) throws UnsupportedEncodingException {
		try {
			return StringUtils.isBlank(path) || path.contains("/images") ? "/" : URLDecoder.decode(path.replaceAll(",",""), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RestApiException(e);
		}

	}
	
	class FileStatus implements Serializable {

		*/
/**
 *
 *//*

		private static final long serialVersionUID = 1L;
		private boolean success = true;
		private String error = null;
		private boolean preventRetry = true;
		public boolean isSuccess() {
			return success;
		}
		public void setSuccess(boolean success) {
			this.success = success;
		}
		public String getError() {
			return error;
		}
		public void setError(String error) {
			this.error = error;
		}
		public boolean isPreventRetry() {
			return preventRetry;
		}
		public void setPreventRetry(boolean preventRetry) {
			this.preventRetry = preventRetry;
		}
		
	}
	
	
	class ImageFile implements Serializable {

		public String getUrl() {
			return url;
		}

		public void setUrl(String url) {
			this.url = url;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getSize() {
			return size;
		}

		public void setSize(String size) {
			this.size = size;
		}

		public boolean isDir() {
			return dir;
		}

		public void setDir(boolean dir) {
			this.dir = dir;
		}

		public String getPath() {
			return path;
		}

		public void setPath(String path) {
			this.path = path;
		}

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		*/
/**
 *
 *//*

		private static final long serialVersionUID = 1L;
		private String url;
		private String name;
		private String size;
		private boolean dir;
		private String path;
		private String id;
	}

}
*/
