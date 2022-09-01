/*
 * © Copyright - Emmanuel Lampe aka. rexlManu 2018.
 */
package de.rexlmanu.bedwars.misc.npc;

/******************************************************************************************
 *    Urheberrechtshinweis                                                       
 *    Copyright © Emmanuel Lampe 2018                                       
 *    Erstellt: 13.07.2018 / 17:02                           
 *
 *    Alle Inhalte dieses Quelltextes sind urheberrechtlich geschützt.                    
 *    Das Urheberrecht liegt, soweit nicht ausdrücklich anders gekennzeichnet,       
 *    bei Emmanuel Lampe. Alle Rechte vorbehalten.                      
 *
 *    Jede Art der Vervielfältigung, Verbreitung, Vermietung, Verleihung,        
 *    öffentlichen Zugänglichmachung oder andere Nutzung           
 *    bedarf der ausdrücklichen, schriftlichen Zustimmung von Emmanuel Lampe.  
 ******************************************************************************************/

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import net.minecraft.server.v1_8_R3.*;
import net.minecraft.server.v1_8_R3.WorldSettings.EnumGamemode;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.util.CraftChatMessage;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public class NPC extends Reflections {

    public static final int STATUS_HURT = 2,
            STATUS_DEATH = 3,
            STATUS_DROWN_DIED = 36,
            STATUS_BURN_DIED = 37,
            STATUS_ITEM_FINISH = 9,
            ANIMATION_SWING_ARM = 0,
            ANIMATION_DAMAGE = 1,
            ANIMATION_CRITICAL_EFFECT = 4,
            ANIMATION_MAGIC_CRITICAL_EFFECT = 5;

    @Getter
    private final int entityID;
    @Getter
    private final Location location;
    private final GameProfile gameprofile;
    private DataWatcher dataWatcher;

    @Setter
    @Getter
    private boolean fire;
    @Setter
    @Getter
    private boolean sneak;
    @Setter
    @Getter
    private boolean sprint;
    @Setter
    @Getter
    private boolean useItem;
    @Setter
    @Getter
    private boolean invisible;

    private final Player player;

    /**
     * constructor for the npc
     *
     * @param name
     * @param uuid
     * @param location
     */
    public NPC(@NonNull final String name, @NonNull final UUID uuid, @NonNull final Location location, final Player player) {
        this.player = player;
        this.entityID = (int) Math.ceil(Math.random() * 1000) + 2000;
        this.gameprofile = new GameProfile(uuid, name);
//        this.changeSkin();
        this.location = location.clone();

        this.fire = false;
        this.sneak = false;
        this.sprint = false;
        this.useItem = false;
        this.invisible = false;
    }

    public void changeSkin() {
        final String value = "eyJ0aW1lc3RhbXAiOjE0NDI4MzY1MTU1NzksInByb2ZpbGVJZCI6IjkwZWQ3YWY0NmU4YzRkNTQ4MjRkZTc0YzI1MTljNjU1IiwicHJvZmlsZU5hbWUiOiJDb25DcmFmdGVyIiwic2lnbmF0dXJlUmVxdWlyZWQiOnRydWUsInRleHR1cmVzIjp7IlNLSU4iOnsidXJsIjoiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS8xMWNlZDMzMjNmYjczMmFjMTc3MTc5Yjg5NWQ5YzJmNjFjNzczZWYxNTVlYmQ1Y2M4YzM5NTZiZjlhMDlkMTIifX19";
        final String signature = "tFGNBQNpxNGvD27SN7fqh3LqNinjJJFidcdF8LTRHOdoMNXcE5ezN172BnDlRsExspE9X4z7FPglqh/b9jrLFDfQrdqX3dGm1cKjYbvOXL9BO2WIOEJLTDCgUQJC4/n/3PZHEG2mVADc4v125MFYMfjzkznkA6zbs7w6z8f7pny9eCWNXPOQklstcdc1h/LvflnR+E4TUuxCf0jVsdT5AZsUYIsJa6fvr0+vItUXUdQ3pps0zthObPEnBdLYMtNY3G6ZLGVKcSGa/KRK2D/k69fmu/uTKbjAWtniFB/sdO0VNhLuvyr/PcZVXB78l1SfBR88ZMiW6XSaVqNnSP+MEfRkxgkJWUG+aiRRLE8G5083EQ8vhIle5GxzK68ZR48IrEX/JwFjALslCLXAGR05KrtuTD3xyq2Nut12GCaooBEhb46sipWLq4AXI9IpJORLOW8+GvY+FcDwMqXYN94juDQtbJGCQo8PX670YjbmVx7+IeFjLJJTZotemXu1wiQmDmtAAmug4U5jgMYIJryXMitD7r5pEop/cw42JbCO2u0b5NB7sI/mr4OhBKEesyC5usiARzuk6e/4aJUvwQ9nsiXfeYxZz8L/mh6e8YPJMyhVkFtblbt/4jPe0bs3xSUXO9XrDyhy9INC0jlLT22QjNzrDkD8aiGAopVvfnTTAug=";
        this.gameprofile.getProperties().put("textures", new Property("textures", value, signature));
    }

    /**
     * change the skin of the entity
     *
     * @param value
     * @param signature
     */
    public void changeSkin(final String value, final String signature) {
        this.gameprofile.getProperties().put("textures", new Property("textures", value, signature));
//        value = "eyJ0aW1lc3RhbXAiOjE0NDI4MzY1MTU1NzksInByb2ZpbGVJZCI6IjkwZWQ3YWY0NmU4YzRkNTQ4MjRkZTc0YzI1MTljNjU1IiwicHJvZmlsZU5hbWUiOiJDb25DcmFmdGVyIiwic2lnbmF0dXJlUmVxdWlyZWQiOnRydWUsInRleHR1cmVzIjp7IlNLSU4iOnsidXJsIjoiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS8xMWNlZDMzMjNmYjczMmFjMTc3MTc5Yjg5NWQ5YzJmNjFjNzczZWYxNTVlYmQ1Y2M4YzM5NTZiZjlhMDlkMTIifX19";
//        signature = "tFGNBQNpxNGvD27SN7fqh3LqNinjJJFidcdF8LTRHOdoMNXcE5ezN172BnDlRsExspE9X4z7FPglqh/b9jrLFDfQrdqX3dGm1cKjYbvOXL9BO2WIOEJLTDCgUQJC4/n/3PZHEG2mVADc4v125MFYMfjzkznkA6zbs7w6z8f7pny9eCWNXPOQklstcdc1h/LvflnR+E4TUuxCf0jVsdT5AZsUYIsJa6fvr0+vItUXUdQ3pps0zthObPEnBdLYMtNY3G6ZLGVKcSGa/KRK2D/k69fmu/uTKbjAWtniFB/sdO0VNhLuvyr/PcZVXB78l1SfBR88ZMiW6XSaVqNnSP+MEfRkxgkJWUG+aiRRLE8G5083EQ8vhIle5GxzK68ZR48IrEX/JwFjALslCLXAGR05KrtuTD3xyq2Nut12GCaooBEhb46sipWLq4AXI9IpJORLOW8+GvY+FcDwMqXYN94juDQtbJGCQo8PX670YjbmVx7+IeFjLJJTZotemXu1wiQmDmtAAmug4U5jgMYIJryXMitD7r5pEop/cw42JbCO2u0b5NB7sI/mr4OhBKEesyC5usiARzuk6e/4aJUvwQ9nsiXfeYxZz8L/mh6e8YPJMyhVkFtblbt/4jPe0bs3xSUXO9XrDyhy9INC0jlLT22QjNzrDkD8aiGAopVvfnTTAug=";
    }

    /**
     * spawn the entity for the player
     */
    public void spawn() {
        final PacketPlayOutNamedEntitySpawn packet = new PacketPlayOutNamedEntitySpawn();

        this.setValue(packet, "a", this.entityID);
        this.setValue(packet, "b", this.gameprofile.getId());
        this.setValue(packet, "c", this.getFixLocation(this.location.getX()));
        this.setValue(packet, "d", this.getFixLocation(this.location.getY()));
        this.setValue(packet, "e", this.getFixLocation(this.location.getZ()));
        this.setValue(packet, "f", this.getFixRotation(this.location.getYaw()));
        this.setValue(packet, "g", this.getFixRotation(this.location.getPitch()));
        this.setValue(packet, "h", 0);
        this.dataWatcher = new DataWatcher(null);
        this.dataWatcher.a(6, (float) 20);
        this.dataWatcher.a(10, (byte) 127);
        this.setValue(packet, "i", this.dataWatcher);
        this.addToTablist();
        this.sendPacket(packet, this.player);
    }


    /**
     * destroy the npc for the player
     */
    public void destroy() {
        final PacketPlayOutEntityDestroy packet = new PacketPlayOutEntityDestroy(this.entityID);
        this.removeFromTablist();
        this.sendPacket(packet, this.player);
    }

    /**
     * add the npc to the tablist for the player
     */
    public void addToTablist() {
        final PacketPlayOutPlayerInfo packet = new PacketPlayOutPlayerInfo();
        final PacketPlayOutPlayerInfo.PlayerInfoData data = packet.new PlayerInfoData(this.gameprofile, 1, EnumGamemode.NOT_SET, CraftChatMessage.fromString(this.gameprofile.getName())[0]);
        @SuppressWarnings("unchecked") final List<PacketPlayOutPlayerInfo.PlayerInfoData> players = (List<PacketPlayOutPlayerInfo.PlayerInfoData>) this.getValue(packet, "b");
        players.add(data);

        this.setValue(packet, "a", PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER);
        this.setValue(packet, "b", players);

        this.sendPacket(packet, this.player);
    }

    /**
     * remove the npc from the tablist
     */
    public void removeFromTablist() {
        final PacketPlayOutPlayerInfo packet = new PacketPlayOutPlayerInfo();
        final PacketPlayOutPlayerInfo.PlayerInfoData data = packet.new PlayerInfoData(this.gameprofile, 1, EnumGamemode.NOT_SET, CraftChatMessage.fromString(this.gameprofile.getName())[0]);
        @SuppressWarnings("unchecked") final List<PacketPlayOutPlayerInfo.PlayerInfoData> players = (List<PacketPlayOutPlayerInfo.PlayerInfoData>) this.getValue(packet, "b");
        players.add(data);

        this.setValue(packet, "a", PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER);
        this.setValue(packet, "b", players);

        this.sendPacket(packet, this.player);
    }

    /**
     * get fixed location
     *
     * @param pos
     * @return
     */
    private int getFixLocation(@NonNull final double pos) {
        return (int) MathHelper.floor(pos * 32.0D);
    }

    /**
     * get fixed rotation
     *
     * @param yawpitch
     * @return
     */
    private byte getFixRotation(@NonNull final float yawpitch) {
        return (byte) ((int) (yawpitch * 256.0F / 360.0F));
    }

    /**
     * play animation clientside
     *
     * @param animation
     */
    public void playAnimation(@NonNull final int animation) {
        final PacketPlayOutAnimation packet = new PacketPlayOutAnimation();
        this.setValue(packet, "a", this.entityID);
        this.setValue(packet, "b", animation);
        this.sendPacket(packet, this.player);
    }

    /**
     * update action like sneaking...
     */
    public void updateAction() {
        this.dataWatcher = new DataWatcher(null);
        byte status = 0;
        status = this.changeMask(status, 0, this.fire); //fire
        status = this.changeMask(status, 1, this.sneak); //sneak
        status = this.changeMask(status, 2, false); //not set
        status = this.changeMask(status, 3, this.sprint); //sprint
        status = this.changeMask(status, 4, this.useItem); //use item
        status = this.changeMask(status, 5, this.invisible); //invisible

        byte skinparts = 0;
        skinparts = this.changeMask(skinparts, 0, true);
        skinparts = this.changeMask(skinparts, 1, true);
        skinparts = this.changeMask(skinparts, 2, true);
        skinparts = this.changeMask(skinparts, 3, true);
        skinparts = this.changeMask(skinparts, 4, true);
        skinparts = this.changeMask(skinparts, 5, true);
        skinparts = this.changeMask(skinparts, 6, true);

        this.dataWatcher.a(0, status);
        this.dataWatcher.a(6, (float) 20);
        this.dataWatcher.a(10, skinparts);
        final PacketPlayOutEntityMetadata packet = new PacketPlayOutEntityMetadata(this.entityID, this.dataWatcher, true);
        this.sendPacket(packet, this.player);
    }

    /**
     * rotate the head
     *
     * @param yaw
     */
    public void rotateHead(@NonNull final float yaw) {
        final PacketPlayOutEntityHeadRotation packet = new PacketPlayOutEntityHeadRotation();
        this.setValue(packet, "a", this.entityID);
        this.setValue(packet, "b", (byte) ((yaw * 256.0F) / 360.0F));
        this.sendPacket(packet, this.player);
    }

    /**
     * teleport the entity to location
     *
     * @param location
     */
    public void teleport(@NonNull final Location location) {
        final PacketPlayOutEntityTeleport packet = new PacketPlayOutEntityTeleport();
        this.setValue(packet, "a", this.entityID);
        this.setValue(packet, "b", this.getFixLocation(location.getX()));
        this.setValue(packet, "c", this.getFixLocation(location.getY()));
        this.setValue(packet, "d", this.getFixLocation(location.getZ()));
        this.setValue(packet, "e", ((byte) this.getFixLocation(location.getYaw())));
        this.setValue(packet, "f", ((byte) this.getFixLocation(location.getPitch())));
        this.setValue(packet, "g", true);
        this.sendPacket(packet, this.player);


        this.headRotation(location.getYaw(), location.getPitch());

    }

    /**
     * rotate the head + body
     *
     * @param yaw
     * @param pitch
     */
    public void headRotation(@NonNull final float yaw, @NonNull final float pitch) {
        final PacketPlayOutEntity.PacketPlayOutEntityLook packet = new PacketPlayOutEntity.PacketPlayOutEntityLook(this.entityID, this.getFixRotation(yaw), this.getFixRotation(pitch), true);
        final PacketPlayOutEntityHeadRotation packetHead = new PacketPlayOutEntityHeadRotation();
        this.setValue(packetHead, "a", this.entityID);
        this.setValue(packetHead, "b", this.getFixRotation(yaw));


        this.sendPacket(packet, this.player);
        this.sendPacket(packetHead, this.player);
    }

    /**
     * play a status (clientside)
     *
     * @param status
     */
    public void status(@NonNull final int status) {
        final PacketPlayOutEntityStatus packet = new PacketPlayOutEntityStatus();
        this.setValue(packet, "a", this.entityID);
        this.setValue(packet, "b", (byte) status);
        this.sendPacket(packet, this.player);
    }

    /**
     * set a item to the slot.
     *
     * @param slot
     * @param itemstack
     */
    public void equip(@NonNull final int slot, @NonNull final ItemStack itemstack) {
        final PacketPlayOutEntityEquipment packet = new PacketPlayOutEntityEquipment();
        this.setValue(packet, "a", this.entityID);
        this.setValue(packet, "b", slot);
        this.setValue(packet, "c", itemstack);
        this.sendPacket(packet, this.player);
    }


    /**
     * only for internal use
     *
     * @param bitMask
     * @param bit
     * @param state
     * @return
     */
    private byte changeMask(@NonNull byte bitMask, @NonNull final int bit, @NonNull final boolean state) {
        if (state) {
            bitMask |= 1 << bit;
        }

        return bitMask;
    }

    /**
     * set the pitch
     *
     * @param pitch
     * @param yaw
     */
    public void setPitch(@NonNull final float pitch, @NonNull final float yaw) {
        final PacketPlayOutEntity.PacketPlayOutEntityLook packet = new PacketPlayOutEntity.PacketPlayOutEntityLook(this.entityID, (byte) yaw, (byte) pitch, true);
        this.sendPacket(packet, this.player);
    }

}
