package theater;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;

/**
 * This class generates a statement for a given invoice of performances.
 */
public class StatementPrinter {
    private final Invoice invoice;
    private final Map<String, Play> plays;

    public StatementPrinter(Invoice invoice, Map<String, Play> plays) {
        this.invoice = invoice;
        this.plays = plays;
    }

    /**
     * Returns a formatted statement of the invoice associated with this printer.
     * 
     * @return the formatted statement
     * @throws RuntimeException if one of the play types is not known
     */
    public String statement() {
        final String customer = getInvoice().getCustomer();
        final StringBuilder result = new StringBuilder("Statement for " + customer + System.lineSeparator());

        for (Performance performance : getInvoice().getPerformances()) {
            final int thisAmount = getAmount(performance);
            final Play play = getPlays().get(performance.getPlayID());
            final String frmtStr = usd(thisAmount);
            final String str = String.format("  %s: %s (%s seats)%n", play.getName(), frmtStr,
                performance.getAudience());
            result.append(str);
        }

        final int totalAmount = getTotalAmount();
        final int volumeCredits = getTotalVolumeCredits();

        result.append(String.format("Amount owed is %s%n", usd(totalAmount)));
        result.append(String.format("You earned %s credits%n", volumeCredits));
        return result.toString();
    }

    /**
     * Calculates the total volume credits for the invoice.
     * @return the total volume credits
     */
    private int getTotalVolumeCredits() {
        int result = 0;
        for (Performance performance : getInvoice().getPerformances()) {
            final Play play = getPlays().get(performance.getPlayID());
            result += getVolumeCredits(performance, play);
        }
        return result;
    }

    /**
     * Calculates the total amount for the invoice.
     * @return the total amount in cents
     */
    private int getTotalAmount() {
        int result = 0;
        for (Performance performance : getInvoice().getPerformances()) {
            result += getAmount(performance);
        }
        return result;
    }

    /**
     * Converts an amount in cents to a US dollar currency string.
     *
     * @param amountCents the amount in cents
     * @return the formatted US dollar string
     */
    private static String usd(int amountCents) {
        return NumberFormat.getCurrencyInstance(Locale.US).format(amountCents / Constants.PERCENT_FACTOR);
    }

    private static int getVolumeCredits(Performance performance, Play play) {
        int result = 0;
        // add volume credits
        result += Math.max(performance.getAudience() - Constants.BASE_VOLUME_CREDIT_THRESHOLD, 0);
        // add extra credit for every five comedy attendees
        if ("comedy".equals(play.getType())) {
            result += performance.getAudience() / Constants.COMEDY_EXTRA_VOLUME_FACTOR;
        }
        return result;
    }

    private int getAmount(Performance performance) {
        final Play play = getPlays().get(performance.getPlayID());
        int result;
        switch (play.getType()) {
            case "tragedy":
                result = Constants.PASTORAL_BASE_AMOUNT;
                if (performance.getAudience() > Constants.TRAGEDY_AUDIENCE_THRESHOLD) {
                    final int audienceDifference = performance.getAudience() - Constants.TRAGEDY_AUDIENCE_THRESHOLD;
                    result += Constants.TRAGEDY_OVER_BASE_CAPACITY_PER_PERSON * audienceDifference;
                }
                break;
            case "comedy":
                result = Constants.COMEDY_BASE_AMOUNT;
                if (performance.getAudience() > Constants.COMEDY_AUDIENCE_THRESHOLD) {
                    result += Constants.COMEDY_OVER_BASE_CAPACITY_AMOUNT
                            + (Constants.COMEDY_OVER_BASE_CAPACITY_PER_PERSON
                                    * (performance.getAudience() - Constants.COMEDY_AUDIENCE_THRESHOLD));
                }
                result += Constants.COMEDY_AMOUNT_PER_AUDIENCE * performance.getAudience();
                break;
            default:
                throw new RuntimeException(String.format("unknown type: %s", play.getType()));
        }
        return result;
    }

    public Invoice getInvoice() {
        return invoice;
    }

    public Map<String, Play> getPlays() {
        return plays;
    }
}
