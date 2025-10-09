package by.yurhilevich;

import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

@Component
public class IndexingScheduler implements SchedulingConfigurer { // <-- ИЗМЕНЕНИЕ: Реализуем интерфейс

    private final IndexerService indexerService;
    private final ConfigService configService;

    public IndexingScheduler(IndexerService indexerService, ConfigService configService) {
        this.indexerService = indexerService;
        this.configService = configService;
    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.addTriggerTask(
                this::checkForUpdatesAndReindex,
                triggerContext -> {
                    int intervalMinutes = configService.getReindexIntervalMinutes();
                    Duration duration = Duration.ofMinutes(intervalMinutes);

                    Date lastCompletion = triggerContext.lastCompletionTime();
                    Instant lastCompletionTime;

                    if (lastCompletion == null) {
                        lastCompletionTime = Instant.now();
                        return lastCompletionTime.plus(Duration.ofMinutes(1));
                    } else {
                        lastCompletionTime = lastCompletion.toInstant();
                    }
                    return lastCompletionTime.plus(duration);
                }
        );
    }

    public void checkForUpdatesAndReindex() {
        System.out.println("Планировщик: Проверка на наличие изменений в файлах...");
        List<String> directories = configService.getSearchDirectories();
        long lastIndexTime = indexerService.getLastIndexTime();
        boolean needsReindexing = false;

        for (String dirPath : directories) {
            if (isDirectoryModified(Paths.get(dirPath), lastIndexTime)) {
                needsReindexing = true;
                break;
            }
        }

        if (needsReindexing) {
            System.out.println("Планировщик: Обнаружены изменения. Запускается переиндексация.");
            indexerService.rebuildIndex();
        } else {
            System.out.println("Планировщик: Изменений не найдено. Индекс актуален.");
        }
    }

    private boolean isDirectoryModified(Path path, long lastIndexTime) {
        if (!Files.exists(path)) return false;
        try (Stream<Path> stream = Files.walk(path)) {
            return stream.map(Path::toFile)
                    .anyMatch(file -> file.lastModified() > lastIndexTime);
        } catch (IOException e) {
            e.printStackTrace();
            return true;
        }
    }
}