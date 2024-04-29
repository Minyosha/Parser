package gb.data;

import com.vaadin.flow.component.textfield.TextArea;
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
    public static String lastLucky;
    public static String currentAdress;

    private static int counter = 0;
    private static int downloaded = 0;
    private static double percentage = 0;

    private static String getHtmlStartSearch;
    private static String getHtmlStartSearchOffset;
    private static String getHtmlEndSearch;
    private static String getHtmlEndSearchOffset;




    public static void runTest(Project project, TextArea consoleTextField) throws HttpStatusException {

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
                        System.out.println(imageLink);
//                            downloadImage(imageLink, article);
                        break;
                    } else {
                        currentText = consoleTextField.getValue();
                        consoleTextField.setValue(currentText + "Image link not found for article " + article.getArticle_content() + "\n");
                        System.out.println("Image link not found for article " + article.getArticle_content());
                    }
                } else {
                    currentText = consoleTextField.getValue();
                    consoleTextField.setValue(currentText + "Product URL not found for article " + article.getArticle_content() + "\n");
                    System.out.println("Product URL not found for article " + article);
                }
            }
            currentText = consoleTextField.getValue();
            consoleTextField.setValue(currentText + "Test completed\n");
            System.out.println("Test completed");
//            break;
        }
    }

    private static void getParams(Project project) {
        getHtmlStartSearch = project.getJsonData("getHtmlStartSearch");
        getHtmlStartSearchOffset = project.getJsonData("getHtmlStartSearchOffset");
        getHtmlEndSearch = project.getJsonData("getHtmlEndSearch");
        getHtmlEndSearchOffset = project.getJsonData("getHtmlEndSearchOffset");
    }


    private static String getImageLink(String productUrl) {
        String html = getHtml(productUrl);
        if (html == null) {
            return null;
        }

        int startIndex = html.indexOf(getHtmlStartSearch) + Integer.parseInt(getHtmlStartSearchOffset);
        System.out.println(startIndex);
        int endIndex = html.indexOf(getHtmlEndSearch, startIndex) + Integer.parseInt(getHtmlEndSearchOffset);
        System.out.println(endIndex);
        if ((startIndex == -1) || (startIndex >= endIndex)) {
            return null;
        }
        if (startIndex + 200 > endIndex) {
            String imageLink = html.substring(startIndex, endIndex);
            System.out.println("передача ссылки");
            System.out.println(imageLink);
            lastLucky = currentAdress;
            System.out.println("Last lucky: " + lastLucky);
            return imageLink;
        }
        System.out.println("не передача ссылки");
        return null;

    }

    private static String getHtml(String url) {
        try {
            // Установка таймаута для HttpURLConnection
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("HEAD");
            connection.setConnectTimeout(5000); // Таймаут соединения в миллисекундах
            connection.setReadTimeout(5000); // Таймаут чтения в миллисекундах
            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                // Установка таймаута для Jsoup
                Document doc = Jsoup.connect(url).timeout(10000).get(); // Таймаут в миллисекундах
                String html = doc.html();
                return html;
            } else {
                System.out.println("Страница не найдена");
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




    public static void run1Test(Project project) throws HttpStatusException {
        String prod = "product/";
        String folderPath = "SE";
        String fileName = "Report.txt";
        Set<Variants> variants = project.getVariants();
        Set<Article> articles = project.getArticles();

        String[] arrayOfStrings = {"https://www.se.com/ww/en/",
                "https://www.se.com/ww/fr/",
                "https://www.se.com/br/pt/"

        };


        try {
            File file = new File(folderPath, fileName);
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;

            while ((line = reader.readLine()) != null) {
                String article = line.trim();
                currentArticle = article;

                if (lastLucky != null) {
                    String imageLink = getImageLink(lastLucky + prod + article);
                    if (imageLink != null) {
                        System.out.println(imageLink);
//                        downloadImage(imageLink, article);
                        downloaded = downloaded - 1;
                    } else {
                        System.out.println("Image link not found for article " + article);
                    }
                } else {
                    System.out.println("Product URL not found for article " + article);
                }

                for (String baseUrl : arrayOfStrings) {
                    String url = baseUrl + prod + article;
                    currentAdress = baseUrl;
                    System.out.println(url);

                    if (url != null) {
                        String imageLink = getImageLink(url);
                        if (imageLink != null) {
                            System.out.println(imageLink);
//                            downloadImage(imageLink, article);
                            System.out.println(imageLink);
                            break;
                        } else {
                            System.out.println("Image link not found for article " + article);
                        }
                    } else {
                        System.out.println("Product URL not found for article " + article);
                    }
                }
                counter = counter + 1;
                System.out.println("Downloaded: " + downloaded);
                System.out.println("Total: " + counter);
                System.out.println("Success rate: " + (downloaded * 100 / counter) + "%");


            }

            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
