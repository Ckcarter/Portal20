#version 150
in vec3 Position;
in vec2 UV0;
in vec4 Color;

uniform mat4 ProjMat;

out vec2 uv01;
out vec2 puv;
out vec4 pColor;


void main() {
    gl_Position = ProjMat * vec4(Position, 1.0);
    uv01 = UV0;
    puv  = UV0 * 2.0 - 1.0;
    pColor = Color;
}
