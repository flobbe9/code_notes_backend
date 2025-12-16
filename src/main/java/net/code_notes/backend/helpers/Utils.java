package net.code_notes.backend.helpers;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.PatternSyntaxException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

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
    
    /** list of file names that should never be deleted during clean up processes */
    public static final Set<String> KEEP_FILES = Set.of(".gitkeep");

    // these strings are defined in "application.yml" under {@code spring.security.oauth2.client.registration.[clientRegistrationId]}
    public static final String OAUTH2_CLIENT_REGISTRATION_ID_GOOGLE = "google";
    public static final String OAUTH2_CLIENT_REGISTRATION_ID_GITHUB = "github";
    public static final String OAUTH2_CLIENT_REGISTRATION_ID_AZURE = "azure";

    public static final String CONFIRM_ACCOUNT_PATH = "/app-user/confirm-account";
    /** Also hard coded in "constants.ts" */
    public static final String RESET_PASSWORD_TOKEN_URL_QUERY_PARAM = "token";
    /** Also hard coded in "constants.ts" */
    public static final String CSRF_TOKEN_URL_QUERY_PARAM = "csrf";
    public static final String LOGIN_PATH = "/login";
    public static final String PRIVACY_POLICY_PATH = "/privacy-policy";
    public static final String CONTACT_PATH = "/contact";
    public static final String RESET_PASSWORD_PATH = "/reset-password-by-token";

    public static final String DEFAULT_SENDER_EMAIL = "noreply.code-notes@gmail.com";

    /** 
     * At least <p>
     * - 8 characters, max 72 (bcrypt max),<p>
     * - one uppercase letter, <p>
     * - one lowercase letter,  <p>
     * - one number and <p>
     * - one of given special characters.
     */
    public static final String PASSWORD_REGEX = "^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[.,;_!#$§%&@€*+=?´`\"'\\{|}\\/()~^-])(.{8,72})$";
    public static final String EMAIL_REGEX = "^[\\w\\-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$";

    public static final String DEFAULT_DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss.SSSSS";

    private static final String AES_ALGORITHM_NAME = "AES/CBC/PKCS5Padding";

    /** The length a search word needs to of in order for the approximate match to use {@code contains} instead of {@code startsWith} */
    public static final int SEARCH_WORD_MIN_LENGTH_FOR_CONTAINS = 5;
    public static final int SEARCH_APPROXIMATE_RATING_POINTS = 1;
    public static final int SEARCH_EXACT_MATCH_RATING_POINTS = 2;
    public static final int SEARCH_ADJACENT_MATCH_RATING_POINTS = 1;


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
     * Convert contents of input stream into a String using {@link Scanner}. Not sure what happens if the contents are not string like, e.g. a picture.
     * 
     * @param is to convert
     * @return converted string
     * @throws IllegalArgumentException
     */
    public static String fileToString(InputStream is) throws IllegalArgumentException {
        assertArgsNotNullAndNotBlankOrThrow(is);

        try (Scanner scanner = new Scanner(is)) {
            StringBuilder stringBuilder = new StringBuilder();

            while (scanner.hasNextLine()) 
                stringBuilder.append(scanner.nextLine());
            
            return stringBuilder.toString();
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
     * @param str
     * @param regex
     * @return {@code true} if each character in {@code str} matches {@code regex}
     * @throws IllegalArgumentException
     */
    public static boolean matchEachChar(String str, String regex) throws IllegalArgumentException, PatternSyntaxException {
        assertArgsNotNullAndNotBlankOrThrow(str, regex);

        for (int i = 0; i < str.length(); i++) {
            String character = Character.toString(str.charAt(i));
            if (!character.matches(regex))
                return false;
        }

        return true;
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
     * Writes given byte array to file.
     * 
     * @param bytes content of file
     * @param fileName complete name of the file
     * @return file or {@code null} if a param is invalid
     * @throws IOException 
     * @throws FileNotFoundException 
     */
    public static File byteArrayToFile(byte[] bytes, String fileName) throws FileNotFoundException, IOException {
        if (assertArgsNullOrBlank(bytes, fileName)) 
            return null;
        
        try (OutputStream fos = new FileOutputStream(fileName)) {
            fos.write(bytes);

            return new File(fileName);
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


    public static boolean isBlank(@Nullable String str) {
        return str == null || str.isBlank();
    }

    public static boolean isEmpty(@Nullable String str) {
        return str == null || str.isEmpty();
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

        try {
            return ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
            
        // possibly no thread-bound request found, happens when called in an asynchronous method
        } catch (IllegalStateException e) {
            return null;
        }
    }


    /**
     * @return the path of the request currently beeing processed or an empty string if no request is available for the current thread
     */
    public static String getReqeustPath() {

        HttpServletRequest request = getCurrentRequest();

        return request == null ? "" : request.getServletPath();
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
     * @param args to check
     * @return {@code true} if at least one arg is {@code null} or blank (will stop iterating), else {@code false}
     */
    public static boolean assertArgsNullOrBlank(Object ...args) throws IllegalArgumentException {
        if (args == null)
            return true;

        for (int i = 0; i < args.length; i++) 
            if (assertNullOrBlank(args[i]))
                return true;

        return false;
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

        assertArgsNotNullAndNotBlankOrThrow(response);

        if (object == null)
            throw new IllegalArgumentException("'object' cannot be null");

        if (object instanceof String)
            response.getWriter().write((String) object);

        else {
            response.getWriter().write(getDefaultObjectMapper().writeValueAsString(object));
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        }
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
     * Overload. Pass a {@link CustomExceptionFormat} with given {@code status} and {@code exception.message} as {@code object} arg. <p>
     * 
     * Will log exception if not {@code null}.
     * 
     * @param response
     * @param status
     * @param message
     * @param exception 
     * @throws JsonProcessingException
     * @throws IOException
     * @throws IllegalArgumentException
     */
    public static void writeToResponse(HttpServletResponse response, HttpStatus status, @Nullable Exception exception) throws JsonProcessingException, IOException, IllegalArgumentException {

        CustomExceptionHandler.logPackageStackTrace(exception);
        writeToResponse(response, status, exception.getMessage());
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


    /**
     * @param rawValue
     * @return the hash (never {@code null})
     * @throws IllegalArgumentException if {@code rawValue} is {@code null}
     * @throws IllegalStateException should not happen
     */
    public static String hashSha256(String rawValue) throws IllegalArgumentException, IllegalStateException {

        if (rawValue == null)
            throw new IllegalArgumentException("'rawValue' cannot be null");

        try {
            MessageDigest cryptoHelper = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = cryptoHelper.digest(rawValue.getBytes(StandardCharsets.UTF_8));
    
            return bytesToHex(hashBytes);

        // should not happen
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }


    /**
     * Will encrypt given string using AES algorithm ({@link #AES_ALGORITHM_NAME}).
     * 
     * @param decryptedStr text to encrypt. Not sure if there's a max length
     * @param keyString needs to be 16 or 32 bytes long
     * @param ivString needs to be 16 bytes long
     * @return encrypted string
     * @throws NoSuchPaddingException
     * @throws NoSuchAlgorithmException
     * @throws InvalidAlgorithmParameterException
     * @throws InvalidKeyException
     * @throws BadPaddingException
     * @throws IllegalBlockSizeException
     */
    public static String encryptAES(String decryptedStr, String keyString, String ivString) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {

        assertArgsNotNullAndNotBlankOrThrow(decryptedStr, keyString, ivString);        

        SecretKey key = new SecretKeySpec(keyString.getBytes(), "AES");
        IvParameterSpec iv = new IvParameterSpec(ivString.getBytes());

        Cipher cipher = Cipher.getInstance(AES_ALGORITHM_NAME);
        cipher.init(Cipher.ENCRYPT_MODE, key, iv);
        byte[] cipherText = cipher.doFinal(decryptedStr.getBytes());
        
        return Base64.getEncoder().encodeToString(cipherText);
    }


    /**
     * Will decrypt given string assuming AES algorithm ({@link #AES_ALGORITHM_NAME}). Use same {@code keyString} and {@code ivString} as for encryption
     * 
     * @param encryptedStr text to decrypt. Expected to be encrypted with AES
     * @param keyString needs to be 16 or 32 bytes long
     * @param ivString needs to be 16 bytes long
     * @return encrypted string
     * @throws NoSuchPaddingException
     * @throws NoSuchAlgorithmException
     * @throws InvalidAlgorithmParameterException
     * @throws InvalidKeyException
     * @throws BadPaddingException
     * @throws IllegalBlockSizeException
     */
    public static String decryptAES(String encryptedStr, String keyString, String ivString) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
                
        assertArgsNotNullAndNotBlankOrThrow(encryptedStr, keyString, ivString);        

        SecretKey key = new SecretKeySpec(keyString.getBytes(), "AES");
        IvParameterSpec iv = new IvParameterSpec(ivString.getBytes());

        Cipher cipher = Cipher.getInstance(AES_ALGORITHM_NAME);
        cipher.init(Cipher.DECRYPT_MODE, key, iv);
        byte[] plainText = cipher.doFinal(Base64.getDecoder().decode(encryptedStr));

        return new String(plainText);
    }


    /**
     * @param bytes
     * @return a concatenated string of each byte converted to hex
     * @throws IllegalArgumentException if bytes is {@code null}
     */
    public static String bytesToHex(byte[] bytes) throws IllegalArgumentException {

        assertArgsNotNullAndNotBlankOrThrow(bytes);

        StringBuilder hexString = new StringBuilder(2 * bytes.length);

        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(0xff & bytes[i]);
            if(hex.length() == 1) 
                hexString.append('0');
            
            hexString.append(hex);
        }
        
        return hexString.toString();
    }

    /**
     * Get a sublist starting at {@code pageIndex * pageSize}.
     * 
     * @param <T>
     * @param list
     * @param pageIndex 0-based, determines the start index. Cannot be negative
     * @param pageSize max length of returned list. Cannot be negative
     * @return a sublist of {@code list}. An empty list if {@code pageIndex} is out of bounds
     * @throws IllegalArgumentException
     */
    @NonNull
    public static<T> List<T> paginate(@NonNull List<T> list, int pageIndex, int pageSize) throws IllegalArgumentException {
        assertArgsNotNullAndNotBlankOrThrow(list);

        if (pageIndex < 0)
            throw new IllegalArgumentException("'pageIndex' must be greater equal 0");
        if (pageSize < 0)
            throw new IllegalArgumentException("'pageSize' must be greater equal 0");

        if (list.isEmpty() || pageSize == 0)
            return new ArrayList<>();

        int startIndex = pageIndex * pageSize;
        int endIndex = startIndex + pageSize;

        // case: pageIndex out of bounds
        if (list.size() < startIndex)
            return new ArrayList<>();

        // case: endIndex out of bounds
        if (list.size() < endIndex)
            endIndex = list.size();

        return list.subList(startIndex, endIndex);
    }

    /**
     * Basically {@code System.getenv()} but with default value that will be returned if {@code key} or {@code System.getenv(key)} are {@code null}.
     * 
     * @param key
     * @param defaultValue
     * @return
     */
    public static String getenv(String key, String defaultValue) {
        if (key == null)
            return defaultValue;

        String value = System.getenv(key);

        return value == null ? defaultValue : value;
    }

    /**
     * Ci indicates that the app is running inside a pipeline or similar. Expect "CI" variable to be defined
     * either as application.property or inside an .env file different then the main ".env".
     * 
     * @return {@code true} or {@code false} (default)
     */
    public static boolean isCI() {
        return System.getProperty("CI", "false").equals("true");
    }

    public static void logHeapSpaceInfo() {
        log.info("-Xmx: {}m", Math.round((Runtime.getRuntime().maxMemory() / Math.pow(1024, 2)) * 100.0) / 100.0); // in MB, 2 fractions
        log.info("Java heap space actual: {}m", Math.round((Runtime.getRuntime().totalMemory() / Math.pow(1024, 2)) * 100.0) / 100.0); // in MB, 2 fractions
        log.info("Java heap space currently used: {}m", Math.round(((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / Math.pow(1024, 2)) * 100.0) / 100.0); // in MB, 2 fractions
    }
}