# Data Limiting
This pair of classes prevents clients from getting chunkbanned when contraptions are too large.

This information is up-to-date as of 1.20.1.

There's a few different packet limits in play:
- the NBT limit: `2_097_152`, enforced by `FriendlyByteBuf.readNbt()`
- the clientbound custom payload limit: `1_048_576` bytes, applies to `ClientboundCustomPayloadPacket`
- the serverbound custom payload limit: `32767` bytes, applies to `ServerboundCustomPayloadPacket`
- the packet limit: `8_388_608` bytes, applies to all packets

# NBT Size vs Bytes
There's two units in play as well - NBT Size and Bytes. The NBT limit uses NBT size, while the other
three use bytes. I'm (TropheusJ) not sure what exactly an NBT Size unit is - it's not bits, but it's
close.

Because of this discrepancy, the NBT limit is actually much lower than it seems. It will usually be
the first limit hit.

Bytes are found by writing a tag to a buffer and getting its `writerIndex`. NBT Size is found using
an `NbtAccounter`.

# Sync
Sync is pretty straightforward.

The only limit relevant here is the clientbound custom payload limit. The NBT limit would be relevant, but
client-side deserialization bypasses it.

Sync is much less of an issue compared to pickup, since a lot of data can be skipped when syncing.

# Pickup
Two limits are relevant for pickup: the NBT limit and the packet limit.

The NBT limit is hit way sooner, and is usually the limiting factor. Other mods may increase it, in
which case the packet limit may become relevant.

The custom payload limit is not relevant since item sync goes through the vanilla `ClientboundContainerSetSlotPacket`.
