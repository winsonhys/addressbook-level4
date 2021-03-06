package seedu.expensetracker.ui;

import static java.time.Duration.ofMillis;
import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static seedu.expensetracker.model.encryption.EncryptionUtil.DEFAULT_ENCRYPTION_KEY;
import static seedu.expensetracker.testutil.EventsUtil.postNow;
import static seedu.expensetracker.testutil.TypicalExpenses.getTypicalExpenses;
import static seedu.expensetracker.testutil.TypicalIndexes.INDEX_SECOND_EXPENSE;
import static seedu.expensetracker.ui.testutil.GuiTestAssert.assertCardDisplaysExpense;
import static seedu.expensetracker.ui.testutil.GuiTestAssert.assertExpenseCardEquals;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;

import guitests.guihandles.ExpenseCardHandle;
import guitests.guihandles.ExpenseListPanelHandle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import seedu.expensetracker.commons.events.ui.JumpToListRequestEvent;
import seedu.expensetracker.commons.util.FileUtil;
import seedu.expensetracker.commons.util.XmlUtil;
import seedu.expensetracker.model.encryption.EncryptionUtil;
import seedu.expensetracker.model.expense.Expense;
import seedu.expensetracker.storage.XmlSerializableExpenseTracker;

public class ExpenseListPanelTest extends GuiUnitTest {
    private static final ObservableList<Expense> TYPICAL_EXPENSES =
            FXCollections.observableList(getTypicalExpenses());

    private static final JumpToListRequestEvent JUMP_TO_SECOND_EVENT = new JumpToListRequestEvent(INDEX_SECOND_EXPENSE);

    private static final Path TEST_DATA_FOLDER = Paths.get("src", "test", "data", "sandbox");

    private static final long CARD_CREATION_AND_DELETION_TIMEOUT = 2500;

    private ExpenseListPanelHandle expenseListPanelHandle;

    @Test
    public void display() {
        initUi(TYPICAL_EXPENSES);

        for (int i = 0; i < TYPICAL_EXPENSES.size(); i++) {
            expenseListPanelHandle.navigateToCard(TYPICAL_EXPENSES.get(i));
            Expense expectedExpense = TYPICAL_EXPENSES.get(i);
            ExpenseCardHandle actualCard = expenseListPanelHandle.getExpenseCardHandle(i);

            assertCardDisplaysExpense(expectedExpense, actualCard);
            assertEquals(Integer.toString(i + 1) + ". ", actualCard.getId());
        }
    }

    @Test
    public void handleJumpToListRequestEvent() {
        initUi(TYPICAL_EXPENSES);
        postNow(JUMP_TO_SECOND_EVENT);
        guiRobot.pauseForHuman();

        ExpenseCardHandle expectedExpense = expenseListPanelHandle.getExpenseCardHandle(
                INDEX_SECOND_EXPENSE.getZeroBased());
        ExpenseCardHandle selectedExpense = expenseListPanelHandle.getHandleToSelectedCard();
        assertExpenseCardEquals(expectedExpense, selectedExpense);
    }

    /**
     * Verifies that creating and deleting large number of expenses in {@code ExpenseListPanel} requires lesser than
     * {@code CARD_CREATION_AND_DELETION_TIMEOUT} milliseconds to execute.
     */
    @Test
    public void performanceTest() throws Exception {
        ObservableList<Expense> backingList = createBackingList(10000);

        assertTimeoutPreemptively(ofMillis(CARD_CREATION_AND_DELETION_TIMEOUT), () -> {
            initUi(backingList);
            guiRobot.interact(backingList::clear);
        }, "Creation and deletion of expense cards exceeded time limit");
    }

    /**
     * Returns a list of expenses containing {@code expenseCount} expenses that is used to populate the
     * {@code ExpenseListPanel}.
     */
    private ObservableList<Expense> createBackingList(int expenseCount) throws Exception {
        Path xmlFile = createXmlFileWithExpenses(expenseCount);
        XmlSerializableExpenseTracker xmlExpenseTracker =
                XmlUtil.getDataFromFile(xmlFile, XmlSerializableExpenseTracker.class);
        return FXCollections.observableArrayList(xmlExpenseTracker.toModelType().decryptTracker(DEFAULT_ENCRYPTION_KEY)
                .getExpenseList());
    }

    /**
     * Returns a .xml file containing {@code expenseCount} expenses. This file will be deleted when the JVM terminates.
     */
    private Path createXmlFileWithExpenses(int expenseCount) throws Exception {
        StringBuilder builder = new StringBuilder();
        builder.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n");
        builder.append("<expensetracker>\n");
        for (int i = 0; i < expenseCount; i++) {
            builder.append("<expenses>\n");
            builder.append("<name>")
                    .append(EncryptionUtil.encryptString(Integer.toString(i) + "a", DEFAULT_ENCRYPTION_KEY))
                    .append("</name>\n");
            builder.append("<category>xFJXCke70UATZt3jQUqA6g==</category>\n");
            builder.append("<cost>2ft68hQdx4gIIV0ubyYq4A==</cost>\n");
            builder.append("<date>SO1fuoCXYqstDP2tg+binw==</date>\n");
            builder.append("</expenses>\n");
        }
        builder.append("<username>manyExpenses</username>\n");
        builder.append("<totalBudget>\n");
        builder.append("<budgetCap>").append((double) expenseCount).append("</budgetCap>\n");
        builder.append("<currentExpenses>").append((double) expenseCount).append("</currentExpenses>\n");
        builder.append("</totalBudget>\n");
        builder.append("</expensetracker>\n");

        Path manyExpensesFile = Paths.get(TEST_DATA_FOLDER + "manyExpenses.xml");
        FileUtil.createFile(manyExpensesFile);
        FileUtil.writeToFile(manyExpensesFile, builder.toString());
        manyExpensesFile.toFile().deleteOnExit();
        return manyExpensesFile;
    }

    /**
     * Initializes {@code expenseListPanelHandle} with a {@code ExpenseListPanel} backed by {@code backingList}.
     * Also shows the {@code Stage} that displays only {@code ExpenseListPanel}.
     */
    private void initUi(ObservableList<Expense> backingList) {
        ExpenseListPanel expenseListPanel = new ExpenseListPanel(backingList);
        uiPartRule.setUiPart(expenseListPanel);

        expenseListPanelHandle = new ExpenseListPanelHandle(getChildNode(expenseListPanel.getRoot(),
                ExpenseListPanelHandle.EXPENSE_LIST_VIEW_ID));
    }
}
