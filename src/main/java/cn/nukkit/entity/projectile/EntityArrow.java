package cn.nukkit.entity.projectile;

import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.level.particle.*;
import cn.nukkit.math.NukkitMath;
import cn.nukkit.math.NukkitRandom;
import cn.nukkit.nbt.tag.*;
import cn.nukkit.network.protocol.AddEntityPacket;
import cn.nukkit.potion.Potion;

/**
 * author: MagicDroidX
 * Nukkit Project
 */
public class EntityArrow extends EntityProjectile {
    public static final int NETWORK_ID = 80;

    public static final int DATA_SOURCE_ID = 17;

    @Override
    public int getNetworkId() {
        return NETWORK_ID;
    }

    @Override
    public float getWidth() {
        return 0.5f;
    }

    @Override
    public float getLength() {
        return 0.5f;
    }

    @Override
    public float getHeight() {
        return 0.5f;
    }

    @Override
    public float getGravity() {
        return 0.05f;
    }

    @Override
    public float getDrag() {
        return 0.01f;
    }

    @Override
    protected double getDamage() {
        return 2;
    }

    protected float gravity = 0.05f;
    protected float drag = 0.01f;

    protected double damage = 2;

    protected boolean isCritical;
    protected int potionId;

    public EntityArrow(FullChunk chunk, CompoundTag nbt) {
        this(chunk, nbt, null);
    }

    public EntityArrow(FullChunk chunk, CompoundTag nbt, Entity shootingEntity) {
        this(chunk, nbt, shootingEntity, false);
    }

    public EntityArrow(FullChunk chunk, CompoundTag nbt, Entity shootingEntity, boolean critical) {
        super(chunk, nbt, shootingEntity);
        this.isCritical = critical;

        if(!this.namedTag.contains("Potion")){
            this.namedTag.putShort("Potion", 0);
        }

        this.potionId = this.namedTag.getShort("Potion");
    }

    @Override
    public boolean onUpdate(int currentTick) {
        if (this.closed) {
            return false;
        }

        this.timings.startTiming();

        boolean hasUpdate = super.onUpdate(currentTick);

        if(this.potionId != 0) {
            int[] color = Potion.getEffect(this.potionId - 1, false).getColor();
            NukkitRandom random = new NukkitRandom();
            this.level.addParticle(new MobSpellParticle(this.add(
                this.getWidth() / 2 + ((double) NukkitMath.randomRange(random, -100, 100)) / 500,
                this.getHeight() / 2 + ((double) NukkitMath.randomRange(random, -100, 100)) / 500,
                this.getWidth() / 2 + ((double) NukkitMath.randomRange(random, -100, 100)) / 500), color[0], color[1], color[2]));
        }

        if (!this.hadCollision && this.isCritical) {
            NukkitRandom random = new NukkitRandom();
            this.level.addParticle(new CriticalParticle(this.add(
                    this.getWidth() / 2 + ((double) NukkitMath.randomRange(random, -100, 100)) / 500,
                    this.getHeight() / 2 + ((double) NukkitMath.randomRange(random, -100, 100)) / 500,
                    this.getWidth() / 2 + ((double) NukkitMath.randomRange(random, -100, 100)) / 500)));
        } else if (this.onGround) {
            this.isCritical = false;
        }

        if (this.age > 1200) {
            this.kill();
            hasUpdate = true;
        }

        this.timings.stopTiming();

        return hasUpdate;
    }

    public int getPotionId(){
        return this.potionId;
    }

    @Override
    public void spawnTo(Player player) {
        AddEntityPacket pk = new AddEntityPacket();
        pk.type = EntityArrow.NETWORK_ID;
        pk.eid = this.getId();
        pk.x = (float) this.x;
        pk.y = (float) this.y;
        pk.z = (float) this.z;
        pk.speedX = (float) this.motionX;
        pk.speedY = (float) this.motionY;
        pk.speedZ = (float) this.motionZ;
        pk.metadata = this.dataProperties;
        player.dataPacket(pk);

        super.spawnTo(player);
    }
}
