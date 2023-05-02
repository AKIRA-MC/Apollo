package com.lunarclient.apollo.module.title;

import com.lunarclient.apollo.Apollo;
import com.lunarclient.apollo.network.NetworkTypes;
import com.lunarclient.apollo.player.AbstractApolloPlayer;
import com.lunarclient.apollo.player.ApolloPlayer;
import com.lunarclient.apollo.title.v1.DisplayTitleMessage;
import com.lunarclient.apollo.title.v1.ResetTitlesMessage;
import com.lunarclient.apollo.title.v1.TitleType;
import lombok.NonNull;

/**
 * Provides the title module.
 *
 * @since 1.0.0
 */
public final class TitleModuleImpl extends TitleModule {

    @Override
    public void displayTitle(@NonNull ApolloPlayer viewer, @NonNull Title title) {
        ((AbstractApolloPlayer) viewer).sendPacket(this.toProtobuf(title));
    }

    @Override
    public void broadcastTitle(@NonNull Title title) {
        DisplayTitleMessage message = this.toProtobuf(title);

        for (ApolloPlayer player : Apollo.getPlayerManager().getPlayers()) {
            ((AbstractApolloPlayer) player).sendPacket(message);
        }
    }

    @Override
    public void resetTitles(@NonNull ApolloPlayer viewer) {
        ((AbstractApolloPlayer) viewer).sendPacket(ResetTitlesMessage.getDefaultInstance());
    }

    private DisplayTitleMessage toProtobuf(Title title) {
        return DisplayTitleMessage.newBuilder()
            .setTitleType(TitleType.forNumber(title.getType().ordinal() + 1))
            .setMessage(NetworkTypes.toProtobuf(title.getMessage()))
            .setScale(title.getScale())
            .setFadeInTime(NetworkTypes.toProtobuf(title.getFadeInTime()))
            .setDisplayTime(NetworkTypes.toProtobuf(title.getDisplayTime()))
            .setFadeOutTime(NetworkTypes.toProtobuf(title.getFadeOutTime()))
            .build();
    }
}
