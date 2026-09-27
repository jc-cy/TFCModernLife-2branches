package com.jccy.tfcmodernlife.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;

/**
 * The electric soup pot's steam, with the exact behaviour the 1.20 addon had: the velocity handed in by the block
 * goes through the vanilla speed constructor, which normalizes it to a total speed of roughly 0.06-0.18 and adds the
 * vanilla 0.1 lift. TFC's own 1.21 steam particle assigns the passed values verbatim (plus a weather wind term)
 * instead, which makes the pot's steam shoot away. Owning the particle keeps upstream particle changes from
 * altering the pot again.
 */
public class SoupPotSteamParticle extends TextureSheetParticle
{
    private SoupPotSteamParticle(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed)
    {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed);
        setAlpha(0.2f);
        setLifetime((int) (12.0f / (random.nextFloat() * 0.9f + 0.1f)));
    }

    @Override
    public ParticleRenderType getRenderType()
    {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static class Provider implements ParticleProvider<SimpleParticleType>
    {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites)
        {
            this.sprites = sprites;
        }

        @Override
        public SoupPotSteamParticle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed)
        {
            final SoupPotSteamParticle particle = new SoupPotSteamParticle(level, x, y, z, xSpeed, ySpeed, zSpeed);
            particle.pickSprite(sprites);
            return particle;
        }
    }
}
