package com.jccy.tfcmodernlife.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;

/**
 * The electric soup pot's bubbles, matching the 1.20 addon: short lived (3-5 ticks) and with no fluid requirement.
 * The vanilla bubble particle - and TFC's 1.21 bubble, which is the vanilla implementation - removes itself as soon
 * as the block it sits in holds no fluid, so bubbles emitted inside the pot disappear on their first tick.
 */
public class SoupPotBubbleParticle extends TextureSheetParticle
{
    private SoupPotBubbleParticle(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed)
    {
        super(level, x, y, z);
        setSize(0.02f, 0.02f);
        quadSize *= random.nextFloat() * 0.6f + 0.2f;
        xd = xSpeed * 0.2 + (Math.random() * 2.0 - 1.0) * 0.02;
        yd = ySpeed * 0.2 + (Math.random() * 2.0 - 1.0) * 0.02;
        zd = zSpeed * 0.2 + (Math.random() * 2.0 - 1.0) * 0.02;
        lifetime = 3 + random.nextInt(3);
    }

    @Override
    public void tick()
    {
        xo = x;
        yo = y;
        zo = z;
        yd += 0.005;
        if (lifetime-- <= 0)
        {
            remove();
            return;
        }
        move(xd, yd, zd);
        xd *= 0.85f;
        yd *= 0.85f;
        zd *= 0.85f;
    }

    @Override
    public ParticleRenderType getRenderType()
    {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    public static class Provider implements ParticleProvider<SimpleParticleType>
    {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites)
        {
            this.sprites = sprites;
        }

        @Override
        public SoupPotBubbleParticle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed)
        {
            final SoupPotBubbleParticle particle = new SoupPotBubbleParticle(level, x, y, z, xSpeed, ySpeed, zSpeed);
            particle.pickSprite(sprites);
            return particle;
        }
    }
}
