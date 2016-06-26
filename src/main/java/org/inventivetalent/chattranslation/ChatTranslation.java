package org.inventivetalent.chattranslation;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.inventivetalent.reflection.annotation.Class;
import org.inventivetalent.reflection.annotation.Field;
import org.inventivetalent.reflection.annotation.ReflectionAnnotations;
import org.inventivetalent.reflection.minecraft.Minecraft;

import java.util.logging.Level;

public class ChatTranslation extends JavaPlugin implements Listener {

	final String TRANSLATED_MESSAGE_PREFIX = "§C§T%%";

	@Class("{nms}.EntityPlayer")
	public java.lang.Class<?>      EntityPlayer;
	@Field(value = "locale",
		   className = "@Class(EntityPlayer)")
	public java.lang.reflect.Field EntityPlayerLocale;

	TranslatorAbstract translator;

	@Override
	public void onEnable() {
		Bukkit.getPluginManager().registerEvents(this, this);
		translator = new GoogleTranslator();
		ReflectionAnnotations.INSTANCE.load(this);
	}

	@EventHandler(priority = EventPriority.LOW)
	public void on(final AsyncPlayerChatEvent event) {
		String message = event.getMessage();
		if (message.startsWith(TRANSLATED_MESSAGE_PREFIX)) {
			event.setMessage(message.substring(TRANSLATED_MESSAGE_PREFIX.length()));
			return;
		} else {
			event.setCancelled(true);
			final String senderLanguage = getLanguage(event.getPlayer());
			for (final Player player : event.getRecipients()) {
				final String receiverLanguage = getLanguage(player);
				TranslationCallback callback = new TranslationCallback() {
					@Override
					public void call(String original, String translation) {
						TextComponent textComponent = new TextComponent(String.format(event.getFormat(), event.getPlayer().getDisplayName(), translation));
						textComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
								new BaseComponent[] {
										new TextComponent("§7" + senderLanguage + " §8| §r" +
												String.format(event.getFormat(), event.getPlayer().getDisplayName(), original)) }));
						player.spigot().sendMessage(textComponent);
					}

					@Override
					public void error(Throwable throwable) {
						player.sendMessage("§cError while translating message");
						player.sendMessage(String.format(event.getFormat(), event.getPlayer().getDisplayName(), event.getMessage()));

						getLogger().log(Level.WARNING, "Unexpected exception while translating (" + event.getPlayer().getName() + ") " + senderLanguage + " -> (" + player.getName() + ") " + receiverLanguage + " '" + event.getMessage() + "'", throwable);
					}
				};
				if (receiverLanguage.equals(senderLanguage)) {
					callback.call(message, message);
					continue;
				}
				translator.translate(message, senderLanguage, receiverLanguage, callback);
			}
		}
	}

	String getLanguage(Player player) {
		try {
			String locale = (String) EntityPlayerLocale.get(Minecraft.getHandle(player));
			return locale.split("_")[0];
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException(e);
		}
	}

}
