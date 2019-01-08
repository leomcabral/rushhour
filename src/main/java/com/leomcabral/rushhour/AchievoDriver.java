package com.leomcabral.rushhour;

import com.leomcabral.rushhour.model.WorkHour;
import io.vavr.control.Either;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class AchievoDriver implements TimesheetDriver {

    private static final String ACHIEVO_URL = "https://horas.daitangroup.com/";

    private final WebDriver driver;
    private final WebDriverWait wait;

    private boolean loggedIn = false;

    public AchievoDriver(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(this.driver, 5);
    }

    @Override
    public void login(String username, String password) {
        driver.get(ACHIEVO_URL);
        WebElement userInput = driver.findElement(By.cssSelector("input[name=\"auth_user\"]"));
        log.info("User input '{}' found", userInput.getTagName());
        userInput.sendKeys(username);

        WebElement pwInput = driver.findElement(By.cssSelector("input[name=\"auth_pw\"]"));
        log.info("Pass input '{}' found", pwInput.getTagName());
        pwInput.sendKeys(password);

        WebElement btn = driver.findElement(By.cssSelector("input[type=\"submit\"].button"));
        btn.click();

//        driver.switchTo().frame("top");
//        WebElement logout = driver.findElement(By.cssSelector(".block > table:nth-child(1) > tbody:nth-child(1) > tr:nth-child(1) > td:nth-child(2) > a:nth-child(2)"));

//        wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(By.name("main")));
//        log.info("Switched to frame main");
//        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("input")));

        loggedIn = true;
    }

    @Override
    public WorkHour getRegisteredHours(LocalDate date) {
        validateLoggedIn();
        navigateToDate(date);
        return parseWorkHourFromTable(date);

    }

    @Override
    public Either<Exception, WorkHour> register(LocalDate date) {
        Either<Exception, WorkHour> result;
        try {
            navigateToDate(date);
            WorkHour generatedWH = generateWorkHour();
            registerHour(generatedWH);
            registerLunchHour(generatedWH);
            result = Either.right(generatedWH);
        } catch (TimeoutException e) {
            result = Either.left(e);
        }

        return result;
    }

    private void navigateToDate(LocalDate date) {
        driver.navigate().refresh();
        wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(By.name("main")));
        log.info("Switched to frame main");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("input")));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("diago")));
        log.info("Inputs on the page");

        Select dayElement = new Select(driver.findElement(By.id("diago")));
        dayElement.selectByValue(String.valueOf(date.getDayOfMonth()));

        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("mesgo")));
        Select monthElement = new Select(driver.findElement(By.id("mesgo")));
        monthElement.selectByValue(padLeft(date.getMonth().getValue()));

        try {
            wait.until(ExpectedConditions.stalenessOf(driver.findElement(By.id("anogo"))));
        } catch(TimeoutException e) {
            //wait.until(ExpectedConditions.stalenessOf(driver.findElement(By.id("anogo"))));
        }
        Select yearElement = new Select(driver.findElement(By.id("anogo")));
        yearElement.selectByValue(String.valueOf(date.getYear()));

        WebElement gotoLink = driver.findElement(By.cssSelector("#togo > a"));
        gotoLink.click();

        wait.until(ExpectedConditions.stalenessOf(driver.findElement(By.id("table_time"))));
    }

    private WorkHour parseWorkHourFromTable(LocalDate date) {
        WebElement tableElement = driver.findElement(By.id("table_time"));
        if (!tableElement.findElements(By.cssSelector("table#table_time tr.green")).isEmpty()) {
            WebElement tr = tableElement.findElement(By.cssSelector("table#table_time tr.green"));
            List<String> data = tr.findElements(By.tagName("td")).stream()
                    .map(WebElement::getText)
                    .collect(Collectors.toList());

            List<Integer> hm = Stream.of(data.get(1).split(":"))
                    .map(Integer::parseInt)
                    .collect(Collectors.toList());
            LocalTime in = LocalTime.of(hm.get(0), hm.get(1));

            List<Integer> durationList = Stream.of(data.get(5).split(":"))
                    .map(Integer::parseInt)
                    .collect(Collectors.toList());

            LocalTime end = in.plusHours(1).plusSeconds(Duration.ofHours(durationList.get(0)).plusMinutes(durationList.get(1)).toSeconds());
            LocalTime intervarlOut = null;
            LocalTime intervalIn = null;

            WorkHour workHour = WorkHour.builder()
                    .registered(true)
                    .date(date)
                    .in(in)
                    .intervalOut(intervarlOut)
                    .intervalIn(intervalIn)
                    .out(end)
                    .build();
            return workHour;
        } else {
            return WorkHour.createNotRegisteredHour(date);
        }
    }

    private WorkHour generateWorkHour() {
        throw new UnsupportedOperationException();
    }

    private void registerHour(WorkHour workHour) {
        throw new UnsupportedOperationException();
    }

    private void registerLunchHour(WorkHour workHour) {
        throw new UnsupportedOperationException();
    }

    private String padLeft(int value) {
        return String.format("%02d", value);
    }

    private void validateLoggedIn() {
        if (!loggedIn) {
            throw new IllegalStateException("Not logged in");
        }
    }
}
