package com.leomcabral.rushhour;

import com.leomcabral.rushhour.model.WorkHour;
import io.vavr.control.Either;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.text.DecimalFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class AchievoDriver implements TimesheetDriver {

    private static final String ACHIEVO_URL = "https://horas.daitangroup.com/";

    private final WebDriver driver;
    private final WebDriverWait wait;
    private final DecimalFormat decimalFormat;

    private boolean loggedIn = false;

    public AchievoDriver(WebDriver driver) {
        this.decimalFormat = new DecimalFormat("00");
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
    public Either<Exception, WorkHour> register(WorkHour workHour) { //crap param
        Either<Exception, WorkHour> result;
        try {
            LocalDate date = workHour.getDate();
            //navigateToDate(date);
            WorkHour generatedWH = generateWorkHour(date);
            log.info("Generated {}", generatedWH);
            registerHour(generatedWH);
            assertRegister("8:00");
            log.info("Hour registered");
            TimeUnit.SECONDS.sleep(1);
            registerLunchHour(generatedWH);
            assertRegister("1:00");
            log.info("Break Registered");
            result = Either.right(generatedWH);
        } catch (TimeoutException | InterruptedException e) {
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
            System.out.println("Registered: " + workHour);
            return workHour;
        } else {
            WorkHour notRegisteredHour = WorkHour.createNotRegisteredHour(date);
            System.out.println("Not Registered: " + notRegisteredHour);
            return notRegisteredHour;
        }
    }

    private WorkHour generateWorkHour(LocalDate date) {
        LocalTime[] range = createRandomValidRange();
        return WorkHour.builder()
                .registered(false)
                .date(date)
                .in(range[0])
                .intervalOut(range[1])
                .intervalIn(range[2])
                .out(range[3])
                .build();
    }

    private LocalTime[] createRandomValidRange() {
        int maxHours = 8;
        int intervalHours = 1;
        int maxScrollWindowMinutes = 1 * 60; // 2 hours

        LocalTime in = LocalTime.of(9, 0);

        int scrollWindowMinutes = new Random(Instant.now().getNano()).nextInt(maxScrollWindowMinutes);
        System.out.println("Scrooling mins: " + scrollWindowMinutes);
        boolean up = new Random(Instant.now().getNano()).nextInt(2) % 2 == 0;
        if (up) {
            in = in.plusMinutes(scrollWindowMinutes);
        } else {
            in = in.minusMinutes(scrollWindowMinutes);
        }

        int intervalVariation = new Random(Instant.now().getNano()).nextInt(30);

        LocalTime intervalOut = in.plusMinutes((3 * 60) + intervalVariation);
        LocalTime intervalIn = intervalOut.plusHours(intervalHours);
        LocalTime out = in.plusHours(9);


        return new LocalTime[]{
                in,
                intervalOut,
                intervalIn,
                out
        };
    }

    private void registerHour(WorkHour workHour) {
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("input")));
        Select dayInput = new Select(driver.findElement(By.id("diai")));
        assert dayInput != null;
        Select monthInput = new Select(driver.findElement(By.id("mesi")));
        assert monthInput != null;
        Select yearInput = new Select(driver.findElement(By.id("anoi")));
        assert yearInput != null;
        Select hourInput = new Select(driver.findElement(By.id("timehH")));
        assert hourInput != null;
        Select minuteInput = new Select(driver.findElement(By.id("timemH")));
        assert minuteInput != null;
        WebElement registryActivity = findSubmitButton(driver, "Register Activity");
        assert registryActivity != null;

        dayInput.selectByValue(String.valueOf(workHour.getDate().getDayOfMonth()));
        monthInput.selectByValue(decimalFormat.format(workHour.getDate().getMonth().getValue()));
        yearInput.selectByValue(String.valueOf(workHour.getDate().getYear()));
        hourInput.selectByValue(String.valueOf(workHour.getIn().getHour()));
        minuteInput.selectByValue(String.valueOf(workHour.getIn().getMinute()));
        registryActivity.click();
    }

    private void registerLunchHour(WorkHour workHour) {
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("input")));
        WebElement registryBreak = findSubmitButton(driver, "Register Break");
        assert registryBreak != null;
        WebElement lunchStart = driver.findElement(By.id("startBreak6"));
        assert lunchStart != null;
        WebElement lunchEnd = driver.findElement(By.id("endBreak6"));
        assert lunchEnd != null;

        String hourOutStr = String.valueOf(workHour.getIntervalOut().getHour());
        String minOutStr = String.valueOf(workHour.getIntervalOut().getMinute());
        String outStr = hourOutStr + minOutStr;
        String hourInStr = String.valueOf(workHour.getIntervalIn().getHour());
        String minInStr = String.valueOf(workHour.getIntervalIn().getMinute());
        String inStr = hourInStr + minInStr;

        lunchStart.sendKeys(outStr);
        lunchEnd.sendKeys(inStr);
        registryBreak.click();
    }

    private String padLeft(int value) {
        return String.format("%02d", value);
    }

    private void validateLoggedIn() {
        if (!loggedIn) {
            throw new IllegalStateException("Not logged in");
        }
    }

    private WebElement findSubmitButton(WebDriver driver, String value) {
        List<WebElement> inputs = driver.findElements(By.tagName("input"));
        for (WebElement element : inputs) {
            if (element.getAttribute("type").equals("submit")) {
                if (element.getAttribute("value").equals(value)) {
                    return element;
                }
                if (element.getAttribute("value").equals(value)) {
                    return element;
                }
            }
        }
        return null;
    }

    private void assertRegister(String text) {
        for (int i = 0; i < 3; i ++) {
            try {
                Thread.sleep(1000);
                List<WebElement> elements = driver.findElements(By.className("yellow"));
                for (WebElement element : elements) {
                    if (element.getText().equals(text)) {
                        return;
                    }
                }
            } catch (StaleElementReferenceException e) {
                driver.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);
                continue;
            } catch (Exception e) {
                break;
            }
        }
        throw new NoSuchElementException("Could not assert if activity was logged");
    }

}
