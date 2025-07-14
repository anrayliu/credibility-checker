import okhttp3.OkHttpClient;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import java.io.IOException;
import java.util.HashMap;
import io.github.cdimascio.dotenv.Dotenv;
import org.json.*;


public class GroqEvaluator {
    static final HashMap<String, String> ALL_MODELS = new HashMap<>() {{
                                                                        put("deepseek", "deepseek-r1-distill-llama-70b");
                                                                        put("gemma", "gemma2-9b-it");
                                                                        put("mistral", "mixtral-8x7b-32768");
                                                                        put("llama", "llama-3.3-70b-versatile");
                                                                    }};

    // needs a groq api key (https://groq.com/)

    static Dotenv dotenv = Dotenv.load();
    static final String API_KEY = dotenv.get("GROQ_KEY");
    static final String REQUEST_URL = "https://api.groq.com/openai/v1/chat/completions";
    static final String ASSISTANT_PROMPT = "You are a website evaluator. Your job is to look at web pages and determine " +
            " if they are credible or not. During all requests, you are to evaluate many aspects of the website, including " +
            "but not limited to, overall recency, author credibility, ux and ads, " +
            "credible and specific citations, accessible and verifiable citations, contact information, working links, purpose and underlying motives, " +
            "typos, potential conflict of interests, links or citations to the webpage from other sources, domain, publisher " +
            "history and credibility, and peer reviews if the website is a study. " +
            "Secondly, it is imperative that you cross reference both general information and specific facts with other sources " +
            "to ensure accuracy. Prioritize cited information." +
            "All aspects should be considered, but verified cross referencing, specific citations, and an unbiased author/publisher " +
            "are especially important. IMPORTANT: issues in these aspects should drastically lower the credibility of the website. " +
            "Be very thorough and strict, but never make anything up." +
            "Finally, give an overall credibility score out of 100 and a one sentence explanation of the key takeaways. " +
            "A score of 90 or above should only be given to webpages that have no issues in the three aspects that I just discussed. " +
            "A webpage with issues in two or more of those aspects should never have a score higher than 50. " +
            "Give the score as a single integer on a new line at the end of your response. " +
            "IMPORTANT: Do not include anything else with the score." +
            "The explanation should be on the line before the rating, beginning with 'The website is'. ";

    static final String TEMPERATURE = "0.2";
    static final String AI_MODEL = ALL_MODELS.get("deepseek");
    static final String WEBPAGE = "https://www.nongmoproject.org/gmo-facts/";

    public static void main(String[] args) throws IOException {
        OkHttpClient client = new OkHttpClient();

        String jsonMessage = "{"
                + "\"model\": \"" + AI_MODEL + "\","
                + "\"temperature\": " + TEMPERATURE + ","
                + "\"messages\": [{"
                + "\"role\": \"user\","
                + "\"content\":" + "\"" + "Please evaluate this website: " + WEBPAGE + "\""
                + "},"
                + "{"
                + "\"role\": \"user\","
                + "\"content\":" + "\"" + ASSISTANT_PROMPT + "\""
                + "}]"
                + "}";

        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonMessage);
        Request request = new Request.Builder()
                .url(REQUEST_URL)
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer " + API_KEY)
                .post(body)
                .build();

        Response response = client.newCall(request).execute();

        JSONObject jsonResponse = new JSONObject(response.body().string());

        String r = jsonResponse.getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content");

        System.out.println("Response Code: " + response.code());

        String[] rSplit = r.strip().split("\n");
        String finalScore = "";
        String summary = "";

        for (int i = rSplit.length - 1; i >= 0; i --) {
            if (!rSplit[i].isBlank()) {
                if (finalScore.isEmpty()) {
                    finalScore = rSplit[i];
                } else {
                    summary = rSplit[i];
                    break;
                }
            }
        }

        finalScore = finalScore.replaceAll("[^0-9]", "");

        System.out.println("Final score: " + finalScore);
        System.out.println("Summary: " + summary);

    }

}
