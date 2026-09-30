attribute vec3 a_position;
attribute vec3 a_normal;
attribute vec2 a_texCoord0;
attribute vec4 a_color;

uniform mat4 u_worldTrans;
uniform mat4 u_projViewTrans;

#ifdef VERTEXCOLOR
varying vec4 v_vertcolor;
#endif
varying vec2 v_texCoord0;

void main() {
    #ifdef VERTEXCOLOR
    #ifdef ACCURATE_VERTEXCOLORS
    // a_color.b;           // still unknown
    // a_color.a;           // still unknown
    float shadow = (a_color.g * 0.5) + 0.5;
    v_vertcolor = vec4(shadow,shadow,shadow,a_color.r);
    #else
    v_vertcolor = a_color;
    #endif
    #endif
    v_texCoord0 = a_texCoord0;
    gl_Position = u_projViewTrans * u_worldTrans * vec4(a_position, 1.0);
}
