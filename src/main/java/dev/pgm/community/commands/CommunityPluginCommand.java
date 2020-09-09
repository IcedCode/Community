package dev.pgm.community.commands;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Dependency;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.Syntax;
import dev.pgm.community.Community;
import dev.pgm.community.CommunityCommand;
import dev.pgm.community.CommunityPermissions;
import dev.pgm.community.moderation.ModerationConfig;
import dev.pgm.community.moderation.feature.ModerationFeature;
import dev.pgm.community.users.feature.UsersFeature;
import dev.pgm.community.utils.CommandAudience;
import dev.pgm.community.utils.ImportUtils;
import dev.pgm.community.utils.ImportUtils.BukkitBanEntry;
import java.util.List;
import net.kyori.text.TextComponent;
import net.kyori.text.format.TextColor;
import tc.oc.pgm.util.UsernameResolver;

// TODO: Maybe move to a different place
@CommandAlias("community")
@Description("Manage the community plugin")
@CommandPermission(CommunityPermissions.RELOAD)
public class CommunityPluginCommand extends CommunityCommand {

  @Dependency private Community plugin;
  @Dependency private ModerationFeature moderation;
  @Dependency private UsersFeature users;

  @Default
  public void reload(CommandAudience audience) {
    plugin.reload();
    audience.sendWarning(TextComponent.of("Community has been reloaded")); // TODO: translate
  }

  @Subcommand("punishments|p")
  public class ModerationAdminCommands extends CommunityCommand {
    @Subcommand("count")
    public void countPunishments(CommandAudience viewer) {
      moderation
          .count()
          .thenAcceptAsync(
              total ->
                  viewer.sendWarning(
                      TextComponent.builder()
                          .append("There are ")
                          .append(TextComponent.of(total))
                          .append(" punishments")
                          .build()));
    }

    
    //TODO: Look into cleaning up messages and redo verbose stuff
    @Subcommand("import")
    @Syntax("[true/false] - Verbose message")
    public void importBans(CommandAudience viewer, @Default("false") boolean verbose) {
      viewer.sendWarning(
          TextComponent.of(
              "Now importing bans to Community database, this may take a while...",
              TextColor.DARK_RED));

      // Get a list of banned players
      List<BukkitBanEntry> bans = ImportUtils.getBukkitBans();
      viewer.sendMessage(
          TextComponent.builder()
              .append("Parsed ")
              .append(Integer.toString(bans.size()), TextColor.GREEN)
              .append(" bans from ")
              .append("banned-players.json", TextColor.AQUA)
              .color(TextColor.GRAY)
              .build());

      bans.stream()
          .forEach(
              ban -> {
                // Save punishment first
                moderation.save(ban.toPunishment((ModerationConfig) moderation.getConfig(), users));

                // Resolve UUID to fetch latest username & update, since profiles may not exist yet
                UsernameResolver.resolve(
                    ban.getUUID(),
                    name -> {
                      users.saveImportedUser(ban.getUUID(), name);
                      if (verbose) {
                        Community.log(
                            "Resolved %s to %s and saved both to database",
                            ban.getUUID().toString(), name);
                      }
                    });
              });
      // After all are added, start resolving usernames
      UsernameResolver.resolveAll();
    }
  }
}
