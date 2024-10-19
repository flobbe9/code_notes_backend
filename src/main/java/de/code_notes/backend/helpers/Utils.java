package de.code_notes.backend.helpers;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;

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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import de.code_notes.backend.controllers.CustomExceptionFormat;
import de.code_notes.backend.controllers.CustomExceptionHandler;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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

    // these strings are defined in "application.yml" under {@code spring.security.oauth2.client.registration.[clientRegistrationId]}
    public static final String OAUTH2_CLIENT_REGISTRATION_ID_GOOGLE = "google";
    public static final String OAUTH2_CLIENT_REGISTRATION_ID_GITHUB = "github";
    public static final String OAUTH2_CLIENT_REGISTRATION_ID_AZURE = "azure";

    public static final String LOGIN_PATH = "/login";
    public static final String CONFIRM_ACCOUNT_PATH = "/app-user/confirm-account";

    /** 
     * At least <p>
     * - 8 characters, max 72 (bcrypt max),<p>
     * - one uppercase letter, <p>
     * - one lowercase letter,  <p>
     * - one number and <p>
     * - one of given special characters.
     */
    public static final String PASSWORD_REGEX = "^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[.,;_!#$%&@€*+=?´`\"'\\/\\{|}()~^-])(.{8,72})$";
    public static final String EMAIL_REGEX = "^[\\w\\-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$";

    public static final String DEFAULT_DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss.SSSSS";

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
     * Default format for a {@link LocalDateTime} with pattern {@code DEFAULT_DATE_TIME_FORMAT + " Z"}.
     * 
     * @param localDateTime to format
     * @return formatted string or {@code ""} if {@code localDateTime} is {@code null}
     */
    public static String formatLocalDateTimeDefault(LocalDateTime localDateTime) {

        if (localDateTime == null)
            return "";

        return ZonedDateTime.now()
                            .format(DateTimeFormatter.ofPattern(DEFAULT_DATE_TIME_FORMAT + " Z"));
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
     * @return the request currently beeing processed
     */
    public static HttpServletRequest getCurrentRequest() {

        return ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
    }


    /**
     * @return the path of the request currently beeing processed
     */
    public static String getReqeustPath() {

        return getCurrentRequest().getServletPath();
    }


    /**
     * Wont throw if given args itself is {@code null}. 
     * 
     * @param args to check
     * @throws IllegalArgumentException
     */
    public static void assertArgsNotNullAndNotBlankOrThrow(Object ...args) throws IllegalArgumentException {

        if (args == null)
            return;

        for (int i = 0; i < args.length; i++) 
            if (assertNullOrBlank(args[i]))
                throw new IllegalArgumentException("Mehtod arg null or blank at index " + i);
    }


    /**
     * @param principal
     * @throws ResponseStatusException 401
     */
    public static void assertPrincipalNotNullAndThrow401(Object principal) throws ResponseStatusException {

        if (principal == null)
            throw new ResponseStatusException(UNAUTHORIZED);
    }


    /**
     * @param obj to check
     * @return {@code true} if given {@code obj} is either {@code null} or (if instance of String) {@link #isBlank(String)}, else {@code false}
     */
    public static boolean assertNullOrBlank(Object obj) {

        if (obj == null)
            return true;

        if (obj instanceof String)
            return isBlank((String) obj);

        return false;
    }


    public static void writeToResponse(HttpServletResponse response, Object object) throws JsonProcessingException, IOException, IllegalArgumentException {

        assertArgsNotNullAndNotBlankOrThrow(response, object);

        response.getWriter().write(getDefaultObjectMapper().writeValueAsString(object));
    }
    

    /**
     * Overload. Pass a {@link CustomExceptionFormat} with given {@code status} and {@code message} as {@code object} arg.
     * 
     * @param response
     * @param status
     * @param message
     * @param doLog if {@code true} both {@code status} and {@code message} will be logged as exception
     * @throws JsonProcessingException
     * @throws IOException
     * @throws IllegalArgumentException
     */
    public static void writeToResponse(HttpServletResponse response, HttpStatus status, String message, boolean doLog) throws JsonProcessingException, IOException, IllegalArgumentException {

        writeToResponse(response, new CustomExceptionFormat(status.value(), message));
        response.setStatus(status.value());

        if (doLog)
            CustomExceptionHandler.logPackageStackTrace(new ResponseStatusException(status, message));
    }

    
    /**
     * Overload. Pass a {@link CustomExceptionFormat} with given {@code status} and {@code message} as {@code object} arg. <p>
     * 
     * Wont log.
     * 
     * @param response
     * @param status
     * @param message
     * @throws JsonProcessingException
     * @throws IOException
     * @throws IllegalArgumentException
     */
    public static void writeToResponse(HttpServletResponse response, HttpStatus status, String message) throws JsonProcessingException, IOException, IllegalArgumentException {

        writeToResponse(response, status, message, false);
    }


    /**
     * Redirect to given location.
     * 
     * @param response
     * @param location the "Location" header value
     * @throws IllegalArgumentException
     * @throws IllegalStateException if given response is already committed
     */
    public static void redirect(HttpServletResponse response, String location) throws IllegalArgumentException, IllegalStateException {

        assertArgsNotNullAndNotBlankOrThrow(response, location);

        if (response.isCommitted())
            throw new IllegalStateException("Response already committed");

        response.setStatus(302);
        response.setHeader("Location", location);
    }


    /**
     * Wont throw.
     * 
     * @param httpStatus
     * @return {@code false} for 4xx and 5xx status, else {@code true} (even if status is invalid)
     */
    public static boolean isHttpStatusAlright(int httpStatus) {

        // case: status invalid, cannot make a decision
        if (httpStatus < 100)
            return true;

        return httpStatus >= 100 && httpStatus <= 399;
    }


    /**
     * Read given env file and get keys and values. Will strip quotes from values and not include any comments, empty lines or any '=' chars.<p>
     * 
     * Key values are expted to be separated with '='.
     * 
     * @param envFileName
     * @return map of key values
     * @throws IOException if file not found
     */
    public static Map<String, String> readEnvFile(String envFileName) throws IOException {

        Map<String, String> envKeyValues = new HashMap<>();

        try (Scanner scanner = new Scanner(new File(envFileName))) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();

                // case: not a key value pair line
                if (!line.contains("=") || line.startsWith("#"))
                    continue;

                int firstEqualsIndex = line.indexOf("=");
                String key = line.substring(0, firstEqualsIndex);
                String value = line.substring(firstEqualsIndex + 1);

                // case: blank value
                if (isBlank(value)) {
                    envKeyValues.put(key, "");
                    continue;
                }

                // remove quotes from value
                Set<Character> quoteChars = Set.of('"', '\'');
                if (quoteChars.contains(value.charAt(0)) || quoteChars.contains(value.charAt(value.length() - 1)))
                    value = value.substring(1, value.length() - 1);

                envKeyValues.put(key, value);
            }
        }

        return envKeyValues;
    }
}