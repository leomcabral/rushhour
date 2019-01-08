package com.leomcabral.rushhour;

import com.leomcabral.rushhour.model.BlacklistLoader;
import com.leomcabral.rushhour.model.WorkHour;
import io.vavr.collection.SortedSet;
import io.vavr.collection.TreeSet;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.io.File;
import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.concurrent.TimeUnit;


@Slf4j
public class Driver {

    public static void main(String[] args) {
//        WebDriver d = new HtmlUnitDriver();
        WebDriver d = createChromeDriver();

//        WebDriver d = createFirefoxDriver();
//        WebDriver d = createPhantomJSDriver();

        d.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);

        TimesheetDriver td = new AchievoDriver(d);
        td.login("lcabral", "gaD88Sa1");

        SortedSet<LocalDate> blacklist = makeBlacklist();


        LocalDate date = LocalDate.of(2017, 8, 1);
        LocalDate today = LocalDate.now();

        try {
            TreeSet.tabulate(10000, (i) -> date.plusDays(i))
                    .takeWhile((ld) -> ld.isBefore(today))
                    .filter(ld -> ld.getDayOfWeek() != DayOfWeek.SUNDAY && ld.getDayOfWeek() != DayOfWeek.SATURDAY)
                    .filter(ld -> !blacklist.contains(ld))
                    .map(td::getRegisteredHours)
                    .filter(wh -> !wh.isRegistered())
                    .forEach(System.out::println);

//        while (date.isBefore(today)) {

//            WorkHour hh = td.getRegisteredHours(date)
//                    .getOrElse(WorkHour.createNotRegisteredHour(date));
//
//            log.info("Work: {}", hh);
//            date = date.plusDays(1);
//        }
        } finally {
            d.close();
            d.quit();
        }

        System.exit(0);
    }

    private static SortedSet<LocalDate> makeBlacklist() {
        try {
            return new BlacklistLoader().load(new FileManager().getFromDataDir("date-blacklist.csv").toPath());
        } catch (IOException e) {
            return TreeSet.empty();
        }
    }

    private static ChromeDriver createChromeDriver() {
        System.setProperty("webdriver.chrome.driver", "/Users/leomcabral/bin/chromedriver");
        return new ChromeDriver();
    }

    private static WebDriver createFirefoxDriver() {
        System.setProperty("webdriver.gecko.driver","/Applications/Firefox.app/Contents/MacOS/firefox");
        return new FirefoxDriver();
    }

    /* DOESN'T WORK */
    private static WebDriver createPhantomJSDriver() {
        File f = new File("/Users/leomcabral/.nvm/versions/node/v10.14.2/bin/phantomjs");
        System.setProperty("phantomjs.binary.path", f.getAbsolutePath());
//        DesiredCapabilities caps = new DesiredCapabilities();
//        caps.setJavascriptEnabled(true);
//        caps.setCapability(PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY, "/Users/leomcabral/.nvm/versions/node/v10.14.2/bin/phantomjs");
        DesiredCapabilities caps = new DesiredCapabilities();
        caps.setJavascriptEnabled(true);
        caps.setCapability("locationContextEnabled", true);
        caps.setCapability("applicationCacheEnabled", true);
        caps.setCapability("browserConnectionEnabled", true);
        caps.setCapability("localToRemoteUrlAccessEnabled", true);
        caps.setCapability("locationContextEnabled", true);
        caps.setCapability("takesScreenshot", true);
        caps.setCapability(PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY,
                f.getAbsolutePath());
        PhantomJSDriver phantomJSDriver = new PhantomJSDriver(caps);
        phantomJSDriver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
        return phantomJSDriver;
    }
}
