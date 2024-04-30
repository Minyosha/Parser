package gb.data;

import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinService;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Set;

public class Operations {
    static String currentArticle;
    public static String currentAdress;

    private static int counter = 0;
    private static int downloaded = 0;
    private static double percentage = 0;

    private static String getHtmlStartSearch;
    private static String getHtmlStartSearchOffset;
    private static String getHtmlEndSearch;
    private static String getHtmlEndSearchOffset;

    private static VaadinRequest currentRequest = VaadinService.getCurrentRequest();
    private static String userAgent = currentRequest.getHeader("User-Agent");


    public static void runTest(Project project, TextArea consoleTextField, int testedArticles) throws HttpStatusException {

        getParams(project);

        consoleTextField.setValue("");
        String currentText = consoleTextField.getValue();
        consoleTextField.setValue(currentText + "Test started\n");
        Set<Variants> variants = project.getVariants();
        Set<Article> articles = project.getArticles();
        boolean runOnce = false;
        int tested = 0;

        for (Article article : articles) {
            if (runOnce) {
                break;
            }
            if (tested == testedArticles) {
                break;
            }
            currentText = consoleTextField.getValue();
            consoleTextField.setValue(currentText + "Trying article: " + article.getArticle_content() + "\n");
            currentArticle = article.getArticle_content();

            for (Variants variant : variants) {
                currentText = consoleTextField.getValue();
                consoleTextField.setValue(currentText + "Trying variant: " + variant.getVariant_content() + "\n");
                String url = variant.getVariant_content() + article.getArticle_content();
                currentText = consoleTextField.getValue();
                consoleTextField.setValue(currentText + "Trying to find image link for article " + article.getArticle_content() + " with URL:\n" + url + "\n");
                if (url != null) {
                    String imageLink = getImageLink(url);
                    if (imageLink != null) {
                        currentText = consoleTextField.getValue();
                        consoleTextField.setValue(currentText + "Link for article " + article.getArticle_content() + " found:\n" + imageLink + "\n");
                        runOnce = true;
                        break;
                    } else {
                        currentText = consoleTextField.getValue();
                        consoleTextField.setValue(currentText + "Image link not found for article: " + article.getArticle_content() + "\n");
                    }
                } else {
                    currentText = consoleTextField.getValue();
                    consoleTextField.setValue(currentText + "Product URL not found for article: " + article.getArticle_content() + "\n");
                }
            }
            tested++;
            currentText = consoleTextField.getValue();
            consoleTextField.setValue(currentText + "Test successfully completed with " + tested + "/" + testedArticles + " articles tested\n");
        }
    }


    public static void startParsing(Project project, TextArea consoleTextField) throws HttpStatusException {

        getParams(project);

        consoleTextField.setValue("");
        String currentText = consoleTextField.getValue();
        consoleTextField.setValue(currentText + "Test started\n");
        Set<Variants> variants = project.getVariants();
        Set<Article> articles = project.getArticles();

        for (Article article : articles) {
            currentText = consoleTextField.getValue();
            consoleTextField.setValue(currentText + "Trying article: " + article.getArticle_content() + "\n");
            currentArticle = article.getArticle_content();

            for (Variants variant : variants) {
                currentText = consoleTextField.getValue();
                consoleTextField.setValue(currentText + "Trying variant: " + variant.getVariant_content() + "\n");
                String url = variant.getVariant_content() + article.getArticle_content();
                currentText = consoleTextField.getValue();
                consoleTextField.setValue(currentText + "Trying to find image link for article " + article.getArticle_content() + " with URL: " + url + "\n");
                if (url != null) {
                    String imageLink = getImageLink(url);
                    if (imageLink != null) {
                        currentText = consoleTextField.getValue();
                        consoleTextField.setValue(currentText + "Link for " + article.getArticle_content() + " found: " + imageLink + "\n");
                    } else {
                        currentText = consoleTextField.getValue();
                        consoleTextField.setValue(currentText + "Image link not found for article " + article.getArticle_content() + "\n");
                    }
                } else {
                    currentText = consoleTextField.getValue();
                    consoleTextField.setValue(currentText + "Product URL not found for article " + article.getArticle_content() + "\n");
                }
            }
            currentText = consoleTextField.getValue();
            consoleTextField.setValue(currentText + "Test completed\n");
            System.out.println("Test completed");
        }

    }


    private static void getParams(Project project) {
        getHtmlStartSearch = project.getJsonData("getHtmlStartSearch");
        getHtmlStartSearchOffset = project.getJsonData("getHtmlStartSearchOffset");
        getHtmlEndSearch = project.getJsonData("getHtmlEndSearch");
        getHtmlEndSearchOffset = project.getJsonData("getHtmlEndSearchOffset");
    }


    private static String getImageLink(String productUrl) {
        String html = getHtml(productUrl, userAgent);
        if (html == null) {
            return null;
        }

        int startIndex = html.indexOf(getHtmlStartSearch) + Integer.parseInt(getHtmlStartSearchOffset);
        int endIndex = html.indexOf(getHtmlEndSearch, startIndex) + Integer.parseInt(getHtmlEndSearchOffset);
        if ((startIndex == -1) || (startIndex >= endIndex)) {
            return null;
        }
        if (startIndex + 200 > endIndex) {
            String imageLink = html.substring(startIndex, endIndex);
            return imageLink;
        }
        return null;

    }

    private static String getHtml(String url, String userAgent) {
        try {
            // Установка таймаута для HttpURLConnection
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("HEAD");
            connection.setConnectTimeout(5000); // Таймаут соединения в миллисекундах
            connection.setReadTimeout(5000); // Таймаут чтения в миллисекундах
            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                // Установка таймаута и User-Agent для Jsoup
                Document doc = Jsoup.connect(url)
                        .timeout(5000) // Таймаут в миллисекундах
                        .userAgent(userAgent) // Использование User-Agent текущего пользователя
                        .get();
                String html = doc.html();
                return html;
            } else {
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


//    private static void downloadImage(String imageLink, String articleNumber) {
//        try {
//            URL url = new URL(imageLink);
//            Path directoryPath = Path.of(DOWNLOAD_DIRECTORY);
//            if (!Files.exists(directoryPath)) {
//                Files.createDirectory(directoryPath);
//            }
//            String fileName = articleNumber + ".zip";
//            Path filePath = directoryPath.resolve(fileName);
//            Files.copy(url.openStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
//            downloaded = downloaded + 1;
//
//        } catch (IOException e) {
//            // Обработка исключения
//            System.out.println("Article number: " + articleNumber);
//            e.printStackTrace();
//        }
//    }



}
