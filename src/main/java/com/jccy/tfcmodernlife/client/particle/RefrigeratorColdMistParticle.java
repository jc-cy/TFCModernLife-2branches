package com.jccy.tfcmodernlife.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

public class RefrigeratorColdMistParticle extends TextureSheetParticle
{
    private static final float BASE_ALPHA = 0.68f;
    private final SpriteSet sprites;
    private float collisionFade = 1f;

    private RefrigeratorColdMistParticle(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, SpriteSet sprites)
    {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed);
        this.sprites = sprites;
        xd = xSpeed + (random.nextDouble() - 0.5D) * 0.01D;
        yd = ySpeed + random.nextDouble() * 0.004D;
        zd = zSpeed + (random.nextDouble() - 0.5D) * 0.01D;
        lifetime = 90 + random.nextInt(45);
        quadSize = 0.48f + random.nextFloat() * 0.2f;
        friction = 0.96f;
        gravity = 0.055f;
        hasPhysics = true;
        setSize(0.28f, 0.28f);
        setColor(1f, 1f, 1f);
        setAlpha(BASE_ALPHA);
        setSpriteFromAge(sprites);
    }

    @Override
    public void tick()
    {
        xo = x;
        yo = y;
        zo = z;
        if (age++ >= lifetime || alpha <= 0.02f)
        {
            remove();
            return;
        }

        xd += (random.nextDouble() - 0.5D) * 0.0015D;
        zd += (random.nextDouble() - 0.5D) * 0.0015D;
        yd -= 0.04D * gravity;

        final double attemptedX = xd;
        final double attemptedY = yd;
        final double attemptedZ = zd;
        final double oldX = x;
        final double oldY = y;
        final double oldZ = z;
        move(attemptedX, attemptedY, attemptedZ);

        final boolean blockedX = collided(oldX, x, attemptedX);
        final boolean blockedY = collided(oldY, y, attemptedY);
        final boolean blockedZ = collided(oldZ, z, attemptedZ);
        if (blockedX || blockedZ)
        {
            xd = (random.nextDouble() - 0.5D) * 0.035D;
            zd = (random.nextDouble() - 0.5D) * 0.035D;
            yd *= 0.3D;
            collisionFade *= 0.9f;
        }
        else
        {
            xd *= friction;
            zd *= friction;
        }

        if (blockedY || onGround)
        {
            yd = 0;
            xd += (random.nextDouble() - 0.5D) * 0.018D;
            zd += (random.nextDouble() - 0.5D) * 0.018D;
            collisionFade *= 0.88f;
        }
        else
        {
            yd *= friction;
        }

        final float progress = Mth.clamp((float) age / (float) lifetime, 0f, 1f);
        final float fadeIn = Mth.clamp(progress / 0.12f, 0f, 1f);
        final float fadeOut = 1f - progress;
        setAlpha(BASE_ALPHA * collisionFade * fadeIn * fadeOut);
        setSpriteFromAge(sprites);
    }

    @Override
    public float getQuadSize(float partialTick)
    {
        final float growth = Mth.clamp((age + partialTick) / 30f, 0.25f, 1f);
        return quadSize * growth;
    }

    @Override
    public ParticleRenderType getRenderType()
    {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    private static boolean collided(double before, double after, double attempted)
    {
        return Math.abs(attempted) > 1.0E-5D && Math.abs(after - before - attempted) > 1.0E-5D;
    }

    public static class Provider implements ParticleProvider<SimpleParticleType>
    {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites)
        {
            this.sprites = sprites;
        }

        @Nullable
        @Override
        public RefrigeratorColdMistParticle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed)
        {
            return new RefrigeratorColdMistParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, sprites);
        }
    }
}
