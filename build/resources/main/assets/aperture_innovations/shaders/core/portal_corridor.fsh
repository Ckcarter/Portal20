#version 150

in vec2 uv01;
in vec2 puv;
in vec4 pColor;
out vec4 fragColor;

uniform float GameTime;

// Parallax params
// camera offset in portal plane
uniform vec2  Parallax;
// spacing between frame slices
uniform float Segment;
// number of slices to draw
uniform int   MaxFrames;
// base frame thickness
uniform float EdgeThickness;
// AA softness multiplier
uniform float EdgeSoftness;
// exponential fade by depth
uniform float DepthFade;
// camera distance from portal
uniform float EyeDist;

// textures
uniform sampler2D Sampler0;
uniform sampler2D Sampler1;
// number of swirl layers (1–16)
uniform int Layers;

// frame exaggeration
const float SegmentThicknessMul = 2.0;
// swirl motion speed
const float BG_TIME_SCALE       = 0.35;
// texture tiling scale
const float BG_SCALE            = 0.3;

// color weights
const vec3 COLORS[16] = vec3[16](
vec3(0.022087,0.098399,0.110818), vec3(0.011892,0.095924,0.089485),
vec3(0.027636,0.101689,0.100326), vec3(0.046564,0.109883,0.114838),
vec3(0.064901,0.117696,0.097189), vec3(0.063761,0.086895,0.123646),
vec3(0.084817,0.111994,0.166380), vec3(0.097489,0.154120,0.091064),
vec3(0.106152,0.131144,0.195191), vec3(0.097721,0.110188,0.187229),
vec3(0.133516,0.138278,0.148582), vec3(0.070006,0.243332,0.235792),
vec3(0.196766,0.142899,0.214696), vec3(0.047281,0.315338,0.321970),
vec3(0.204675,0.390010,0.302066), vec3(0.080955,0.314821,0.661491)
);

// 2D rotation
mat2 rotZ(float a) {
    float s = sin(a), c = cos(a);
    return mat2(c, -s, s, c);
}

// Swirl transform per layer (rotation + scale + drift)
vec2 end_portal_layer_uv(vec2 uv, float layer) {
    float angle = radians((layer*layer*4321.0 + layer*9.0) * 2.0);
    mat2  R     = rotZ(angle);
    float S     = (4.5 - layer / 4.0) * 2.0;
    vec2  T     = vec2(
    17.0 / layer,
    (2.0 + layer / 1.5) * (GameTime * (1.5 * BG_TIME_SCALE))
    );

    vec2 u = R * (uv * S) + T;
    return u * 0.5 + vec2(0.25);
}

// Accumulate layered portal background at UV
vec3 sample_end_portal(vec2 uv) {
    vec3 c = texture(Sampler0, uv).rgb * COLORS[0];

    int L = clamp(Layers, 1, 16);
    for (int i = 0; i < 16; ++i) {
        if (i >= L) break;
        float layer = float(i + 1);
        vec2 luv = end_portal_layer_uv(uv, layer);
        c += texture(Sampler1, luv).rgb * COLORS[i];
    }

    return c;
}

void main() {
    // Ray setup in portal plane

    // ray origin
    vec2  Oxy = Parallax;
    // ray direction
    vec2  Vxy = puv - Oxy;

    float Ez  = max(EyeDist, 1e-6);

    // Slab intersection against portal box
    float fwPortal = max(fwidth(puv.x), fwidth(puv.y));
    float ext = 1.0 + 2.0 * fwPortal * EdgeSoftness;

    const float BIG = 1e19;
    float tmin_x, tmax_x;
    if (abs(Vxy.x) > 1e-8) {
        float invx = 1.0 / Vxy.x;
        float tx1 = (-ext - Oxy.x) * invx;
        float tx2 = ( ext - Oxy.x) * invx;
        tmin_x = min(tx1, tx2);
        tmax_x = max(tx1, tx2);
    } else {
        tmin_x = (abs(Oxy.x) <= ext) ? -BIG : BIG;
        tmax_x = (abs(Oxy.x) <= ext) ?  BIG : -BIG;
    }

    float tmin_y, tmax_y;
    if (abs(Vxy.y) > 1e-8) {
        float invy = 1.0 / Vxy.y;
        float ty1 = (-ext - Oxy.y) * invy;
        float ty2 = ( ext - Oxy.y) * invy;
        tmin_y = min(ty1, ty2);
        tmax_y = max(ty1, ty2);
    } else {
        tmin_y = (abs(Oxy.y) <= ext) ? -BIG : BIG;
        tmax_y = (abs(Oxy.y) <= ext) ?  BIG : -BIG;
    }

    float tEnter = max(max(tmin_x, tmin_y), 1.0);
    float tExit  = min(tmax_x, tmax_y);


    // Background sampled from furthest visible slice

    vec3 col;

    if (tEnter < tExit) {
        float dt    = Segment / Ez;
        float t0    = tEnter + 1e-4;
        float tLast = t0 + float(MaxFrames - 1) * dt;
        float tBG   = min(tExit, tLast);

        vec2 pBG  = Oxy + tBG * Vxy;
        vec2 uvBG = fract((pBG * 0.5 + 0.5) * BG_SCALE);
        col = sample_end_portal(uvBG);
    } else {
        // Fallback when ray misses box
        vec2 uvBG = fract(uv01 * BG_SCALE);
        col = sample_end_portal(uvBG);
    }


    // Corridor frames composited front-to-back

    if (tEnter < tExit) {
        float dt = Segment / Ez;
        float t0 = tEnter + 1e-4;

        for (int i = 0; i < MaxFrames; ++i) {
            float t = t0 + float(i) * dt;
            vec2 p = Oxy + t * Vxy;

            // Box visibility with AA
            float ax = abs(p.x), ay = abs(p.y);
            float fw = max((fwidth(p.x) + fwidth(p.y)) * EdgeSoftness, 1e-4);
            float vis = (1.0 - smoothstep(0.0, fw, ax - 1.0)) *
            (1.0 - smoothstep(0.0, fw, ay - 1.0));

            // Edge band near borders
            float thK = (EdgeThickness * SegmentThicknessMul) / pow(max(t, 1e-6), 0.7);
            float edgeX = 1.0 - smoothstep(thK, thK + fw, 1.0 - ax);
            float edgeY = 1.0 - smoothstep(thK, thK + fw, 1.0 - ay);
            float edge = max(edgeX, edgeY);

            // Depth based attenuation
            float z = Ez * (t - 1.0);
            float fade = exp(-DepthFade * z);

            float frame = vis * edge * fade;

            // Blend frame over background
            col = mix(col, pColor.rgb, clamp(frame, 0.0, 1.0));
        }
    }

    fragColor = vec4(clamp(col, 0.0, 1.0), 1.0);
}
