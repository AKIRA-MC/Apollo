package com.lunarclient.apollo.module.waypoint;

import com.lunarclient.apollo.common.location.ApolloBlockLocation;
import com.lunarclient.apollo.event.player.ApolloRegisterPlayerEvent;
import com.lunarclient.apollo.network.NetworkTypes;
import com.lunarclient.apollo.option.config.Serializer;
import com.lunarclient.apollo.player.AbstractApolloPlayer;
import com.lunarclient.apollo.player.ApolloPlayer;
import com.lunarclient.apollo.waypoint.v1.DisplayWaypointMessage;
import com.lunarclient.apollo.waypoint.v1.RemoveWaypointMessage;
import com.lunarclient.apollo.waypoint.v1.ResetWaypointsMessage;
import java.awt.Color;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import lombok.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

/**
 * Provides the waypoints module.
 *
 * @since 1.0.0
 */
public final class WaypointModuleImpl extends WaypointModule implements Serializer {

    /**
     * Creates a new instance of {@link WaypointModuleImpl}.
     *
     * @since 1.0.0
     */
    public WaypointModuleImpl() {
        super();
        this.serializer(Waypoint.class, new WaypointSerializer());
        this.handle(ApolloRegisterPlayerEvent.class, this::onPlayerRegister);
    }

    @Override
    public void displayWaypoint(@NonNull ApolloPlayer viewer, @NonNull Waypoint waypoint) {
        ((AbstractApolloPlayer) viewer).sendPacket(this.toProtobuf(waypoint));
    }

    @Override
    public void removeWaypoint(@NonNull ApolloPlayer viewer, @NonNull String waypointName) {
        ((AbstractApolloPlayer) viewer).sendPacket(RemoveWaypointMessage.newBuilder()
            .setName(waypointName)
            .build());
    }

    @Override
    public void removeWaypoint(@NonNull ApolloPlayer viewer, @NonNull Waypoint waypoint) {
        this.removeWaypoint(viewer, waypoint.getName());
    }

    @Override
    public void resetWaypoints(@NonNull ApolloPlayer viewer) {
        ((AbstractApolloPlayer) viewer).sendPacket(ResetWaypointsMessage.getDefaultInstance());
    }

    private void onPlayerRegister(ApolloRegisterPlayerEvent event) {
        ApolloPlayer player = event.getPlayer();
        List<Waypoint> waypoints = this.getOptions().get(player, WaypointModule.DEFAULT_WAYPOINTS);

        if (waypoints != null) {
            for (Waypoint waypoint : waypoints) {
                ((AbstractApolloPlayer) player).sendPacket(this.toProtobuf(waypoint));
            }
        }
    }

    private DisplayWaypointMessage toProtobuf(Waypoint waypoint) {
        return DisplayWaypointMessage.newBuilder()
            .setName(waypoint.getName())
            .setLocation(NetworkTypes.toProtobuf(waypoint.getLocation()))
            .setColor(NetworkTypes.toProtobuf(waypoint.getColor()))
            .setPreventRemoval(waypoint.isPreventRemoval())
            .setVisible(waypoint.isVisible())
            .build();
    }

    private static final class WaypointSerializer implements TypeSerializer<Waypoint> {
        @Override
        public Waypoint deserialize(Type type, ConfigurationNode node) throws SerializationException {
            return Waypoint.builder()
                .name(this.virtualNode(node, "name").getString())
                .location(ApolloBlockLocation.builder()
                    .world(this.virtualNode(node, "location", "world").getString())
                    .x(this.virtualNode(node, "location", "x").getInt())
                    .y(this.virtualNode(node, "location", "y").getInt())
                    .z(this.virtualNode(node, "location", "z").getInt())
                    .build()
                )
                .color(Color.decode(this.virtualNode(node, "color").getString("#FFFFFF")))
                .preventRemoval(this.virtualNode(node, "prevent-removal").getBoolean())
                .visible(this.virtualNode(node, "visible").getBoolean())
                .build();
        }

        @Override
        public void serialize(Type type, @Nullable Waypoint waypoint, ConfigurationNode node) throws SerializationException {
            if(waypoint == null) {
                node.raw(null);
                return;
            }

            node.node("name").set(waypoint.getName());
            node.node("location", "world").set(waypoint.getLocation().getWorld());
            node.node("location", "x").set(waypoint.getLocation().getX());
            node.node("location", "y").set(waypoint.getLocation().getY());
            node.node("location", "z").set(waypoint.getLocation().getZ());
            node.node("color").set(String.format("#%06X", (0xFFFFFF & waypoint.getColor().getRGB())));
            node.node("prevent-removal").set(waypoint.isPreventRemoval());
            node.node("visible").set(waypoint.isVisible());
        }

        private ConfigurationNode virtualNode(ConfigurationNode source, Object... path) throws SerializationException {
            if(!source.hasChild(path)) throw new SerializationException("Required field " + Arrays.toString(path) + " not found!");
            return source.node(path);
        }

    }

}
