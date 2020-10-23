/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.xwiki.test.ui.po;

import java.util.List;
import java.util.regex.Pattern;

import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.test.ui.po.editor.WYSIWYGEditPage;

/**
 * Represents the common actions possible on all Pages when using the "view" action.
 * 
 * @version $Id$
 * @since 3.2M3
 */
public class ViewPage extends BasePage
{
    @FindBy(id = "xwikicontent")
    private WebElement content;

    @FindBy(id = "hierarchy")
    private WebElement breadcrumbElement;

    private BreadcrumbElement breadcrumb;

    /**
     * Opens the comments tab.
     * 
     * @return element for controlling the comments tab
     */
    public CommentsTab openCommentsDocExtraPane()
    {
        getDriver().findElement(By.id("Commentslink")).click();
        waitForDocExtraPaneActive("comments");
        return new CommentsTab();
    }

    public HistoryPane openHistoryDocExtraPane()
    {
        getDriver().findElement(By.id("Historylink")).click();
        waitForDocExtraPaneActive("history");
        return new HistoryPane();
    }

    public AttachmentsPane openAttachmentsDocExtraPane()
    {
        getDriver().findElement(By.id("Attachmentslink")).click();
        waitForDocExtraPaneActive("attachments");
        return new AttachmentsPane();
    }

    public InformationPane openInformationDocExtraPane()
    {
        getDriver().findElement(By.id("Informationlink")).click();
        waitForDocExtraPaneActive("information");
        return new InformationPane();
    }

    /** @return does this page exist. */
    public boolean exists()
    {
        List<WebElement> messages = getDriver().findElementsWithoutWaiting(By.className("xwikimessage"));
        for (WebElement message : messages) {
            if (message.getText().contains("The requested page could not be found.")
                || message.getText().contains("The page has been deleted.")) {
                return false;
            }
        }
        return true;
    }

    /**
     * @return the page's main content as text (no HTML)
     */
    public String getContent()
    {
        return this.content.getText();
    }

    public WYSIWYGEditPage editSection(int sectionNumber)
    {
        By sectionBy = By.cssSelector("a.edit_section[href*=\"section=" + sectionNumber + "\"]");

        // Since Section Edit links are generated by JS (for XWiki Syntax 2.0) after the page has loaded make sure
        // we wait for them.
        getDriver().waitUntilElementIsVisible(sectionBy);

        getDriver().findElement(sectionBy).click();
        return new WYSIWYGEditPage();
    }

    /**
     * Clicks on a wanted link in the page.
     */
    public void clickWantedLink(String spaceName, String pageName, boolean waitForTemplateDisplay)
    {
        clickWantedLink(new DocumentReference("xwiki", spaceName, pageName), waitForTemplateDisplay);
    }

    /**
     * Clicks on a wanted link in the page.
     *
     * @since 7.2M2
     */
    public void clickWantedLink(EntityReference reference, boolean waitForTemplateDisplay)
    {
        WebElement brokenLink = getDriver().findElement(
            By.xpath("//span[@class='wikicreatelink']/a[contains(@href,'/create/" + getUtil().getURLFragment(reference)
                + "')]"));
        brokenLink.click();
        if (waitForTemplateDisplay) {
            // Ensure that the template choice popup is displayed. Since this is done using JS we need to wait till
            // it's displayed. For that we wait on the Create button since that would mean the template radio buttons
            // will all have been displayed.
            getDriver().waitUntilElementIsVisible(By.xpath("//div[@class='modal-popup']//input[@type='submit']"));
        }
    }

    public BreadcrumbElement getBreadcrumb()
    {
        if (this.breadcrumb == null) {
            this.breadcrumb = new BreadcrumbElement(this.breadcrumbElement);
        }
        return this.breadcrumb;
    }

    public String getBreadcrumbContent()
    {
        return getBreadcrumb().getPathAsString();
    }

    public boolean hasBreadcrumbContent(String breadcrumbItem, boolean isCurrent)
    {
        return hasBreadcrumbContent(breadcrumbItem, isCurrent, true);
    }

    public boolean hasBreadcrumbContent(String breadcrumbItem, boolean isCurrent, boolean withLink)
    {
        return getBreadcrumb().hasPathElement(breadcrumbItem, isCurrent, withLink);
    }

    /**
     * Clicks on the breadcrumb link with the given text.
     * 
     * @param linkText the link text
     * @return the target of the breadcrumb link
     */
    public ViewPage clickBreadcrumbLink(String linkText)
    {
        getBreadcrumb().clickPathElement(linkText);

        return new ViewPage();
    }

    public boolean isInlinePage()
    {
        return getDriver().findElements(By.xpath("//form[@id = 'inline']")).size() > 0;
    }

    /**
     * @param paneId valid values: "history", "comments", etc
     */
    public void waitForDocExtraPaneActive(String paneId)
    {
        getDriver().waitUntilElementIsVisible(By.id(paneId + "content"));
    }

    /**
     * Waits until the page has the passed content by refreshing the page
     * 
     * @param expectedValue the content value to wait for (in regex format)
     * @since 4.0M1
     */
    public void waitUntilContent(final String expectedValue)
    {
        // Using an array to have an effectively final variable.
        final String[] lastContent = new String[1];
        try {
            getDriver().waitUntilCondition(new ExpectedCondition<Boolean>() {
                private Pattern pattern = Pattern.compile(expectedValue, Pattern.DOTALL);

                @Override
                public Boolean apply(WebDriver driver)
                {
                    driver.navigate().refresh();
                    lastContent[0] = getContent();
                    return Boolean.valueOf(pattern.matcher(lastContent[0]).matches());
                }
            });
        } catch (TimeoutException e) {
            throw new TimeoutException(String.format("Got [%s]\nExpected [%s]", lastContent[0], expectedValue), e);
        }
    }

    /**
     * @param elementLocator the element to locate in the content of the page.
     * @return true if the content of the page contains the element
     * @since 11.5RC1
     */
    public boolean contentContainsElement(By elementLocator)
    {
        return getDriver().hasElementWithoutWaiting(this.content, elementLocator);
    }

    private void useShortcutForDocExtraPane(String shortcut, String pane)
    {
        getDriver().createActions().sendKeys(shortcut).perform();
        waitForDocExtraPaneActive(pane);
    }

    public AttachmentsPane useShortcutKeyForAttachmentPane()
    {
        useShortcutForDocExtraPane("a", "attachments");
        return new AttachmentsPane();
    }

    public HistoryPane useShortcutKeyForHistoryPane()
    {
        useShortcutForDocExtraPane("h", "history");
        return new HistoryPane();
    }

    public CommentsTab useShortcutKeyForCommentPane()
    {
        useShortcutForDocExtraPane("c", "comments");
        return new CommentsTab();
    }

    public InformationPane useShortcutKeyForInformationPane()
    {
        useShortcutForDocExtraPane("i", "information");
        return new InformationPane();
    }
}
