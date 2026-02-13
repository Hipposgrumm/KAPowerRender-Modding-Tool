#ifdef GL_ES
precision mediump float;
#endif

#ifdef HAS_TEXTURE
uniform sampler2D u_texture;
    #if WRAP_X==4
    #define BORDER_X
    #endif // WRAP_X==4
    #if WRAP_Y==4
    #define BORDER_Y
    #endif // WRAP_Y==4

    #if defined(BORDER_X) || defined(BORDER_Y)
    uniform vec4 u_bordercolor;
    #endif // defined(BORDER_X) || defined(BORDER_Y)

    #ifdef ALPHATEST
    uniform float u_alpharef;
    #endif // ALPHATEST
#else
uniform vec4 u_color;
#endif // HAS_TEXTURE

varying vec4 v_vertcolor;
varying vec2 v_texCoord0;

#ifdef HAS_TEXTURE
    // https://cycling74.com/forums/how-to-set-texture-wrap-modes-in-glsl-shader-code#reply-628826add28262336fe793f9
    #if WRAP_X != 0 && !defined(BORDER_X)
    float wrapX(float val) {
        #if WRAP_X == 1 // WRAP
        return mod(val, 1.0);
        #elif WRAP_X == 2 // MIRROR
        return 1.0 - abs(mod(val, 2.0) - 1.0);
        #elif WRAP_X == 3 // CLAMP
        return clamp(val, 0.0, 1.0);
        #elif WRAP_X == 5 // MIRROR_ONCE
        return clamp(abs(val), 0.0, 1.0);
        #else
        return val;
        #endif
    }
    #endif // WRAP_X != 0 && !defined(BORDER_X)
    #if WRAP_Y != 0 && !defined(BORDER_Y)
    float wrapY(float val) {
        #if WRAP_Y == 1 // WRAP
        return mod(val, 1.0);
        #elif WRAP_Y == 2 // MIRROR
        return 1.0 - abs(mod(val, 2.0) - 1.0);
        #elif WRAP_Y == 3 // CLAMP
        return clamp(val, 0.0, 1.0);
        #elif WRAP_Y == 5 // MIRROR_ONCE
        return clamp(abs(val), 0.0, 1.0);
        #else
        return val;
        #endif
    }
    #endif // WRAP_Y != 0 && !defined(BORDER_Y)

    #if defined(BORDER_X) || defined(BORDER_Y)
    bool useBorder(vec2 texcoord) {
        #ifdef BORDER_X
            #ifdef BORDER_Y
            return (clamp(texcoord.x, 0.0, 1.0) != texcoord.x) ||
                    (clamp(texcoord.y, 0.0, 1.0) != texcoord.y);
            #else
            return clamp(texcoord.x, 0.0, 1.0) != texcoord.x;
            #endif // BORDER_Y
        #else
            #ifdef BORDER_Y
            return clamp(texcoord.y, 0.0, 1.0) != texcoord.y;
            #else
            return false; // Should be impossible.
            #endif // BORDER_Y
        #endif // BORDER_X
    }
    #endif // defined(BORDER_X) || defined(BORDER_Y)
#endif // HAS_TEXTURE

vec2 getTexcoord() {
    #ifdef HAS_TEXTURE
        #if WRAP_X!=0 && !defined(BORDER_X)
            #if WRAP_Y!=0 && !defined(BORDER_Y)
            return vec2(wrapX(v_texCoord0.x), wrapY(v_texCoord0.y));
            #else
            return vec2(wrapX(v_texCoord0.x), v_texCoord0.y);
            #endif // WRAP_Y!=0 && !defined(BORDER_Y)
        #else
            #if WRAP_Y!=0 && !defined(BORDER_Y)
            return vec2(v_texCoord0.x, wrapY(v_texCoord0.y));
            #else
            return v_texCoord0;
            #endif // WRAP_Y!=0 && !defined(BORDER_Y)
        #endif // WRAP_X!=0 && !defined(BORDER_X)]
    #else
    return v_texCoord0;
    #endif // HAS_TEXTURE
}

vec4 getTexColor(vec2 texcoord, out bool ignoreAlphatest) {
    #ifdef HAS_TEXTURE
        #if defined(BORDER_X) || defined(BORDER_Y)
        if (useBorder(texcoord)) {
            ignoreAlphatest = true;
            return u_bordercolor;
        }
        #endif // defined(BORDER_X) || defined(BORDER_Y)
        ignoreAlphatest = false;
        return texture2D(u_texture, texcoord);
    #else
    return u_color;
    #endif // HAS_TEXTURE
}

void main() {
    vec2 texcoord = getTexcoord();

    bool ignoreAlphatest;
    vec4 col = getTexColor(texcoord, ignoreAlphatest);
    #if defined(HAS_TEXTURE) && defined(ALPHATEST)
    if (!ignoreAlphatest && col.a <= u_alpharef) discard;
    #endif // defined(HAS_TEXTURE) && defined(ALPHATEST)

    col.a *= v_vertcolor.a;
    gl_FragColor = col;
}
