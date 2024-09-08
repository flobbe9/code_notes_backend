package de.code_notes.backend.helpers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import lombok.extern.log4j.Log4j2;


/**
 * Util class holding static helper methods.
 * 
 * @since 0.0.1
 */
@Log4j2
@Configuration
public class Utils {
    
    public static final String RESOURCES_FOLDER = "src/main/resources/";
    public static final String STATIC_FOLDER = RESOURCES_FOLDER + "/static";
    public static final String MAIL_FOLDER = RESOURCES_FOLDER + "mail/";
    public static final String IMG_FOLDER = RESOURCES_FOLDER + "img/";

    public static final String VERIFICATION_MAIL_FILE_NAME = "verificationMail.html";
    public static final String FAVICON_FILE_NAME = "favicon.png"; 

    /** list of file names that should never be deleted during clean up processes */
    public static final Set<String> KEEP_FILES = Set.of(".gitkeep");

    /** 
     * At least <p>
     * - 8 characters, max 30,<p>
     * - one uppercase letter, <p>
     * - one lowercase letter,  <p>
     * - one number and <p>
     * - one of given special characters.
     */
    public static final String PASSWORD_REGEX = "^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[.,;_!#$%&@€*+=?´`\"'\\/\\{|}()~^-])(.{8,30})$";
    public static final String EMAIL_REGEX = "^[\\w\\-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$";

    @Bean
    File verificationMail() {

        return new File(MAIL_FOLDER + VERIFICATION_MAIL_FILE_NAME);
    }

    @Bean
    File favicon() {

        return new File(IMG_FOLDER + FAVICON_FILE_NAME);
    }


    /**
     * Convert file into String using {@link BufferedReader}.
     * 
     * @param file to convert
     * @return converted string
     * @throws IOException 
     * @throws FileNotFoundException 
     */
    public static String fileToString(File file) throws FileNotFoundException, IOException {
        
        // read to string
        try (Reader fis = new FileReader(file);
             BufferedReader br = new BufferedReader(fis)) {
            StringBuilder stringBuilder = new StringBuilder();

            String line = null;
            while ((line = br.readLine()) != null)
                stringBuilder.append(line);

            String str = stringBuilder.toString();
            return replaceOddChars(str);
        }
    }


    /**
     * Write given string to given file.
     * 
     * @param str to write to file
     * @param file to write the string to
     * @return the file
     * @throws IOException 
     */
    public static File stringToFile(String str, File file) throws IOException {

        try (BufferedWriter br = new BufferedWriter(new FileWriter(file))) {
            br.write(str);

            return file;
        }
    }


    /**
     * Replace odd characters that java uses for special chars like 'ä, ö, ü, ß' etc. with original chars. <p>
     * 
     * Does not alter given String.
     * 
     * @param str to fix
     * @return fixed string
     */
    public static String replaceOddChars(String str) {

        // alphabetic
        str = str.replace("Ã?", "Ä");
        str = str.replace("Ã¤", "ä");
        str = str.replace("Ã¶", "ö");
        str = str.replace("Ã¼", "ü");
        str = str.replace("ÃŸ", "ß");

        // special chars
        str = str.replace("â?¬", "€");

        return str;
    }
    

    /**
     * Prepends a '/' to given String if there isn't already one.
     * 
     * @param str String to prepend the slash to
     * @return sring with "/" prepended or just "/" if given string is null. Does not alter given str
     */
    public static String prependSlash(String str) {

        if (str == null || str.equals(""))
            return "/";

        return str.charAt(0) == '/' ? str : "/" + str;
    }


    /**
     * @param password to validate
     * @return true matches regex and not null, else false
     * @see {@link #PASSWORD_REGEX}
     */
    public static boolean isPasswordValid(String password) {

        if (isBlank(password))
            return false;

        return password.matches(PASSWORD_REGEX);
    }


    /**
     * @param email to validate
     * @return true matches regex and not null, else false
     * @see {@link #EMAIL_REGEX}
     */
    public static boolean isEmailValid(String email) {

        if (isBlank(email))
            return false;

        return email.matches(EMAIL_REGEX);
    }


    /**
     * Prepends current date and time to given string. Replace ':' with '-' due to .docx naming conditions.
     * 
     * @param str String to format
     * @return current date and time plus str
     */
    public static String prependDateTime(String str) {

        return LocalDateTime.now().toString().replace(":", "-") + "_" + str;
    }


    /**
     * Writes given byte array to file into {@link #STATIC_FOLDER}.
     * 
     * @param bytes content of file
     * @param fileName name of the file
     * @return file or {@code null} if a param is invalid
     * @throws IOException 
     * @throws FileNotFoundException 
     */
    public static File byteArrayToFile(byte[] bytes, String fileName) throws FileNotFoundException, IOException {

        String completeFileName = STATIC_FOLDER + prependSlash(fileName);

        if (bytes == null) 
            return null;
        
        try (OutputStream fos = new FileOutputStream(completeFileName)) {
            fos.write(bytes);

            return new File(completeFileName);
        }
    }


    /**
     * Read given file to byte array.
     * 
     * @param file to read
     * @return byte array
     * @throws IOException 
     */
    public static byte[] fileToByteArray(File file) throws IOException {

        return Files.readAllBytes(file.toPath());
    }


    public static boolean isKeepFile(File file) {

        return KEEP_FILES.contains(file.getName());
    }
    

    public static boolean isInteger(String str) {

        try {
            Integer.parseInt(str);

            return true;

        } catch (NumberFormatException e) {
            return false;
        }
    }


    /**
     * @param object to convert to json string
     * @return given object as json string
     * @throws JsonProcessingException 
     */
    public static String objectToJson(Object object) throws JsonProcessingException {

        ObjectWriter objectWriter = getDefaultObjectMapper().writer().withDefaultPrettyPrinter();

        return objectWriter.writeValueAsString(object);
    }


    /**
     * @param millis time to convert in milli seconds
     * @param timeZone to use for conversion, i.e. {@code "UTC"} or {@code "Europe/Berlin"}. If invalid, system default will be used.
     * @return given time as {@link LocalDateTime} object or null if {@code millis} is invalid
     */
    public static LocalDateTime millisToLocalDateTime(long millis, @Nullable String timeZone) {

        ZoneId zoneId;
        try {
            zoneId = ZoneId.of(timeZone);

        // case: invalid timeZone
        } catch (DateTimeException | NullPointerException e) {
            zoneId = ZoneId.systemDefault();
        }

        Instant instant = Instant.ofEpochMilli(millis);
        return LocalDateTime.ofInstant(instant, zoneId);
    }


    /**
     * Execute given {@code runnable} asynchronously inside a thread.
     * 
     * @param runnable lambda function without parameter or return value
     */
    public static void runInsideThread(Runnable runnable) {

        ExecutorService executorService = Executors.newFixedThreadPool(1);
        executorService.submit(runnable);
    }


    /**
     * Execute given {@code callable} asynchronously inside a thread.
     * 
     * @param T return type of {@code callable}
     * @param callable lambda function without parameter
     * 
     */
    public static <T> void runInsideThread(Callable<T> callable) {

        ExecutorService executorService = Executors.newFixedThreadPool(1);
        executorService.submit(callable);
    }


    public static boolean isBlank(String str) {

        return str == null || str.isBlank();
    }


    /**
     * Default format for a {@link LocalDateTime} with pattern {@code "yyyy-MM-dd HH:mm:ss:SS Z"}.
     * 
     * @param localDateTime to format
     * @return formatted string or {@code ""} if {@code localDateTime} is {@code null}
     */
    public static String formatLocalDateTimeDefault(LocalDateTime localDateTime) {

        if (localDateTime == null)
            return "";

        return ZonedDateTime.now()
                            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss:SS Z"));
    }


    /**
     * @return new object mapper instance that can handle {@link LocalDate} and {@link LocalDateTime}
     */
    public static ObjectMapper getDefaultObjectMapper() {

        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules();
        mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd"));

        return mapper;
    }


    /**
     * @return the path of the request currently beeing processed
     */
    public static String getReqeustPath() {

        return ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
            .getRequest()
            .getServletPath();
    }
}