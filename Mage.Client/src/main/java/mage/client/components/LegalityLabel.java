package mage.client.components;

import mage.cards.decks.Deck;
import mage.cards.decks.DeckValidator;
import mage.cards.decks.importer.DeckImporter;
import org.unbescape.html.HtmlEscape;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.Map;

/**
 * @author Elandril
 */
public class LegalityLabel extends JLabel {

    protected static final Color COLOR_UNKNOWN = new Color(174, 174, 174);
    protected static final Color COLOR_LEGAL = new Color(117, 152, 110);
    protected static final Color COLOR_NOT_LEGAL = new Color(191, 84, 74);
    protected static final Color COLOR_TEXT = new Color(255, 255, 255);
    protected static final Dimension DIM_MINIMUM = new Dimension(75, 25);
    protected static final Dimension DIM_MAXIMUM = new Dimension(150, 75);
    protected static final Dimension DIM_PREFERRED = new Dimension(75, 25);

    protected Deck currentDeck;
    protected String errorMessage;
    protected DeckValidator validator;

    /**
     * Creates a <code>LegalityLabel</code> instance with the specified text
     * and the given DeckValidator.
     *
     * @param text      The text to be displayed by the label.
     * @param validator The DeckValidator to check against.
     */
    public LegalityLabel(String text, DeckValidator validator) {
        super(text);
        this.validator = validator;

        setBackground(COLOR_UNKNOWN);
        setForeground(COLOR_TEXT);
        setHorizontalAlignment(SwingConstants.CENTER);
        setMinimumSize(DIM_MINIMUM);
        setMaximumSize(DIM_MAXIMUM);
        setName(text); // NOI18N
        setOpaque(true);
        setPreferredSize(DIM_PREFERRED);
    }

    /**
     * Creates a <code>LegalityLabel</code> instance with the given DeckValidator and uses its
     * shortName as the text.
     *
     * @param validator The DeckValidator to check against.
     */
    public LegalityLabel(DeckValidator validator) {
        this(validator.getShortName(), validator);
    }

    /**
     * Creates a <code>LegalityLabel</code> instance with no DeckValidator or text.
     * This is used by the Netbeans GUI Editor.
     */
    public LegalityLabel() {
        super();

        setBackground(COLOR_UNKNOWN);
        setForeground(COLOR_TEXT);
        setHorizontalAlignment(SwingConstants.CENTER);
        setMinimumSize(DIM_MINIMUM);
        setMaximumSize(DIM_MAXIMUM);
        setOpaque(true);
        setPreferredSize(DIM_PREFERRED);
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public DeckValidator getValidator() {
        return validator;
    }

    public void setValidator(DeckValidator validator) {
        this.validator = validator;
        revalidateDeck();
    }

    protected String formatInvalidTooltip(Map<String, String> invalid) {
        return invalid.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .reduce("<html><body><p>Deck is <span style='color:#BF544A;font-weight:bold;'>INVALID</span></p><u>The following problems have been found:</u><br><table>",
                        (str, entry) -> String.format("%s<tr><td><b>%s</b></td><td>%s</td></tr>", str, HtmlEscape.escapeHtml5(entry.getKey()), HtmlEscape.escapeHtml5(entry.getValue())), String::concat)
                + "</table></body></html>";
    }

    private String appendErrorMessage(String string) {
        if (errorMessage.isEmpty())
            return string;
        if (string.contains("<html>")) {
            return string.replaceFirst("((</body>)?</html>)", String.format("<br><br>The following errors occurred while loading the deck:<br>%s$1", HtmlEscape.escapeHtml5(errorMessage)));
        }
        return string.concat("\n\nThe following errors occurred while loading the deck:\n" + errorMessage);
    }

    public void showState(Color color) {
        setBackground(color);
    }

    public void showState(Color color, String tooltip) {
        setBackground(color);
        setToolTipText(appendErrorMessage(tooltip));
    }

    public void showStateUnknown(String tooltip) {
        showState(COLOR_UNKNOWN, tooltip);
    }

    public void showStateLegal(String tooltip) {
        showState(COLOR_LEGAL, tooltip);
    }

    public void showStateNotLegal(String tooltip) {
        showState(COLOR_NOT_LEGAL, tooltip);
    }

    public void validateDeck(Deck deck) {
        errorMessage = "";
        currentDeck = deck;
        if (deck == null) {
            showStateUnknown("<html><body><b>No deck loaded!</b></body></html>");
            return;
        }
        if (validator == null) {
            showStateUnknown("<html><body><b>No deck type selected!</b></body></html>");
            return;
        }
        try {
            if (validator.validate(deck)) {
                showStateLegal("<html><body>Deck is <span style='color:green;font-weight:bold;'>VALID</span></body></html>");
            } else {
                showStateNotLegal(formatInvalidTooltip(validator.getInvalid()));
            }
        } catch (Exception e) {
            showStateUnknown(String.format("<html><body><b>Deck could not be validated!</b><br>The following error occurred while validating this deck:<br>%s</body></html>", HtmlEscape.escapeHtml5(e.getMessage())));
        }
    }

    public void validateDeck(File deckFile) {
        deckFile = deckFile.getAbsoluteFile();
        if (!deckFile.exists()) {
            errorMessage = String.format("Deck file '%s' does not exist.", deckFile.getAbsolutePath());
            showStateUnknown("<html><body><b>No Deck loaded!</b></body></html>");
            return;
        }
        try {
            StringBuilder errorMessages = new StringBuilder();
            Deck deck = Deck.load(DeckImporter.importDeckFromFile(deckFile.getAbsolutePath(), errorMessages), true, true);
            errorMessage = errorMessages.toString();
            validateDeck(deck);
        } catch (Exception ex) {
            errorMessage = String.format("Error importing deck from file '%s'!", deckFile.getAbsolutePath());
        }
    }

    public void revalidateDeck() {
        validateDeck(currentDeck);
    }

    public void validateDeck(String deckFile) {
        validateDeck(new File(deckFile));
    }
}
