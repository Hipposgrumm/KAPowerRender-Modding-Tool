#ifdef GL_ES
precision mediump float;
#endif

#ifdef HAS_TEXTURE
uniform sampler2D u_texture;
    #if WRAP_X==4 || WRAP_Y==4 // BORDER
    uniform vec4 u_bordercolor;
    #endif
#endif

varying vec4 v_vertcolor;
varying vec2 v_texCoord0;

#ifdef HAS_TEXTURE
    // https://cycling74.com/forums/how-to-set-texture-wrap-modes-in-glsl-shader-code#reply-628826add28262336fe793f9
    #if WRAP_X != 0 && WRAP_X != 4 // BORDER
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
    #endif // WRAP_X != 0 && WRAP_X != 4
    #if WRAP_Y != 0 && WRAP_Y != 4 // BORDER
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
    #endif // WRAP_Y != 0 && WRAP_Y != 4
#endif // HAS_TEXURE

void main() {
    #ifdef HAS_TEXTURE
        #if WRAP_X!=0 && WRAP_X!=4
            #if WRAP_Y!=0 && WRAP_Y!=4
            vec2 texcoord = vec2(wrapX(v_texCoord0.x), wrapY(v_texCoord0.y));
            #else
            vec2 texcoord = vec2(wrapX(v_texCoord0.x), v_texCoord0.y);
            #endif // WRAP_Y!=0 && WRAP_Y!=4
        #else
            #if WRAP_Y!=0 && WRAP_Y!=4
            vec2 texcoord = vec2(v_texCoord0.x, wrapY(v_texCoord0.y));
            #else
            vec2 texcoord = v_texCoord0;
            #endif // WRAP_Y!=0 && WRAP_Y!=4
        #endif // WRAP_X!=0 && WRAP_X!=4

        #if WRAP_X == 4 // BORDER
            #if WRAP_Y == 4 // BORDER
            if (
                (clamp(texcoord.x, 0, 1) != texcoord.x) ||
                (clamp(texcoord.y, 0, 1) != texcoord.y)
            ) {
                gl_FragColor = u_bordercolor;
            } else {
                gl_FragColor = texture2D(u_texture, texcoord);
            }
            #else
            if (clamp(texcoord.x, 0, 1) != texcoord.x) {
                gl_FragColor = u_bordercolor;
            } else {
                gl_FragColor = texture2D(u_texture, texcoord);
            }
            #endif // WRAP_Y!=4
        #else
            #if WRAP_Y == 4 // BORDER
            if (clamp(texcoord.y, 0, 1) != texcoord.y) {
                gl_FragColor = u_bordercolor;
            } else {
                gl_FragColor = texture2D(u_texture, texcoord);
            }
            #else
            gl_FragColor = texture2D(u_texture, texcoord);
            #endif // WRAP_Y!=4
        #endif // WRAP_X!=4
    #else
    gl_FragColor = vec4(1,1,1,1);
    #endif // HAS_TEXTURE
}
