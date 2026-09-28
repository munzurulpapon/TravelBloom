package com.travelbloom.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

/**
 * Sends the "Welcome to TravelBloom" confirmation email using Resend
 * (https://resend.com) — free tier: 100 emails/day, 3,000/month, no
 * credit card needed.
 *
 * SETUP:
 *   1. Sign up free at https://resend.com and grab an API key from
 *      the dashboard (starts with "re_").
 *   2. Paste it into RESEND_API_KEY below.
 *   3. FROM_ADDRESS below uses Resend's shared sandbox address
 *      ("onboarding@resend.dev"), which works immediately with ZERO
 *      setup and can send to any real inbox. Once you verify your own
 *      domain in the Resend dashboard, change FROM_ADDRESS to something
 *      like "TravelBloom <hello@yourdomain.com>" for a more professional
 *      sender name.
 *
 * This never throws where it's called from — registration must succeed
 * even if the email fails to send (no internet, bad key, rate limit,
 * etc). Call sendWelcomeEmailAsync() so it never blocks the UI thread.
 */
public class EmailService {

    // TODO: paste your free Resend API key here — https://resend.com/api-keys
    private static final String RESEND_API_KEY = "";
    // Works out of the box with zero setup. Swap for your own verified
    // domain later, e.g. "TravelBloom <hello@travelbloom.app>".
    private static final String FROM_ADDRESS = "TravelBloom <onboarding@resend.dev>";

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[\\w.+-]+@[\\w-]+\\.[a-zA-Z]{2,}$");

    private final HttpClient httpClient = HttpClient.newHttpClient();


    public boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email.trim()).matches();
    }


    /**
     * Fire-and-forget: sends the welcome email on a background thread so
     * signup never waits on network I/O. Silently does nothing if the
     * key isn't set yet or the email address looks invalid.
     */
    public void sendWelcomeEmailAsync(String toEmail, String username) {

        if (!isValidEmail(toEmail)) {
            return;
        }

        Thread thread = new Thread(() -> sendWelcomeEmail(toEmail, username));
        thread.setDaemon(true);
        thread.start();
    }


    private void sendWelcomeEmail(String toEmail, String username) {

        if (RESEND_API_KEY == null || RESEND_API_KEY.isBlank()) {
            System.out.println(
                    "Resend API key not set — skipping welcome email to " + toEmail
                            + " (see EmailService.java to enable this)."
            );
            return;
        }

        try {

            String html = buildWelcomeHtml(username);

            String jsonBody = "{"
                    + "\"from\":\"" + escape(FROM_ADDRESS) + "\","
                    + "\"to\":[\"" + escape(toEmail) + "\"],"
                    + "\"subject\":\"Congratulations — Welcome to TravelBloom! 🌸\","
                    + "\"html\":\"" + escape(html) + "\""
                    + "}";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.resend.com/emails"))
                    .header("Authorization", "Bearer " + RESEND_API_KEY)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request, HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                System.out.println("Welcome email sent to " + toEmail);
            } else {
                System.out.println(
                        "Resend API returned status " + response.statusCode()
                                + ": " + response.body()
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private String buildWelcomeHtml(String username) {

        return "<div style=\"font-family:Segoe UI, Arial, sans-serif; background-color:#f4f7fb; padding:32px;\">"
                + "<div style=\"max-width:480px; margin:0 auto; background:white; border-radius:16px; overflow:hidden; box-shadow:0 4px 16px rgba(20,40,60,0.10);\">"
                + "<div style=\"background:linear-gradient(to right, #1E88E5, #14B8A6); padding:28px; text-align:center;\">"
                + "<div style=\"font-size:32px;\">&#127804;</div>"
                + "<div style=\"color:white; font-size:22px; font-weight:bold; margin-top:6px;\">Welcome to TravelBloom</div>"
                + "</div>"
                + "<div style=\"padding:28px;\">"
                + "<p style=\"font-size:15px; color:#1b2b3a;\">Hi " + escapeHtml(username) + ",</p>"
                + "<p style=\"font-size:14px; color:#55636f; line-height:1.6;\">"
                + "Congratulations — your TravelBloom account has been created successfully! "
                + "You're all set to start planning your next journey: build itineraries, track your budget, "
                + "manage transport and stays, and get a smart, at-a-glance readiness report for every trip."
                + "</p>"
                + "<p style=\"font-size:14px; color:#55636f; line-height:1.6;\">Happy travels!<br/>— The TravelBloom Team</p>"
                + "</div>"
                + "</div>"
                + "</div>";
    }

    private String escapeHtml(String text) {
        return text == null ? "" : text.replace("<", "&lt;").replace(">", "&gt;");
    }

    private String escape(String text) {
        return text == null ? "" : text.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "");
    }
}
