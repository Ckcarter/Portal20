#version 150

uniform sampler2D Sampler0;

uniform vec4 ColorModulator;

uniform float Points[30];
uniform int pointAmount;
uniform float brighten;
uniform float radius;

in vec2 texCoord0;
in vec4 vertexColor;

out vec4 fragColor;

void main() {
    vec4 color = texture(Sampler0, texCoord0) * vertexColor * ColorModulator;
    if(color.a < 0.1)
    {
        discard;
    }

    vec3 fragPos = vec3(texCoord0.x, texCoord0.y, 0.0);
    float totalBrightnessIncrease = 0.0;

    for (int i = 0; i < pointAmount; i++) {
        if(i > 10)
        {
            break;
        }
        vec3 pointPos = vec3(Points[i], Points[i+1], Points[i+2]);

        float dist = distance(fragPos, pointPos);

        if (dist < 2.0) {
            float intensity = 1.0 - (dist / radius);
            intensity = smoothstep(0.0, 1.0, intensity);

            totalBrightnessIncrease += intensity * brighten;
        }
    }

    color.rgb = color.rgb * (1.0 + totalBrightnessIncrease);

    color.rgb = clamp(color.rgb, 0.0, 1.0);

    fragColor = color;
}
