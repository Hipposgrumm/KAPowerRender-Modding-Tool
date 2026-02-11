attribute vec3 a_position;
attribute vec3 a_normal;
attribute vec2 a_texCoord0;
attribute vec4 a_color;

uniform mat4 u_worldTrans;
uniform mat4 u_projViewTrans;

varying vec4 v_vertcolor;
varying vec2 v_texCoord0;

void main() {
    v_vertcolor = a_color;
    v_texCoord0 = a_texCoord0;
    gl_Position = u_projViewTrans * u_worldTrans * vec4(a_position, 1.0);
}
