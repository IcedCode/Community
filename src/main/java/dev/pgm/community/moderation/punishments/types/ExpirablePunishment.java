package dev.pgm.community.moderation.punishments.types;

import dev.pgm.community.moderation.ModerationConfig;
import dev.pgm.community.moderation.punishments.Punishment;
import dev.pgm.community.moderation.punishments.PunishmentBase;
import dev.pgm.community.usernames.UsernameService;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;

/** A punishment that can expire * */
public abstract class ExpirablePunishment extends PunishmentBase {

  private Duration duration;

  public ExpirablePunishment(
      UUID id,
      UUID targetId,
      Optional<UUID> issuerId,
      String reason,
      Instant timeIssued,
      Duration length,
      boolean active,
      Instant lastUpdated,
      Optional<UUID> lastUpdatedBy,
      ModerationConfig config,
      UsernameService usernames) {
    super(
        id,
        targetId,
        issuerId,
        reason,
        timeIssued,
        active,
        lastUpdated,
        lastUpdatedBy,
        config,
        usernames);
    this.duration = length;
  }

  public Duration getDuration() {
    return duration;
  }

  public static @Nullable Duration getDuration(Punishment punishment) {
    return punishment instanceof ExpirablePunishment
        ? ExpirablePunishment.class.cast(punishment).getDuration()
        : null;
  }
}