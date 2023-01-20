#version 330 core

in vec3 i_position;
in vec2 i_uv;
in vec3 i_normal;
in float i_objectid;

out vec2 v_uv;
out vec3 v_normal;
flat out uint v_objectid;
out vec3 v_worldpos;

uniform float u_time;
uniform float u_y;

mat2 rot2D(float a) {
	float c = cos(a), s = sin(a);
	return mat2(c, s, -s, c);
}

/*
Do not read this code for too long, it may hurt

I did not want to spend much time on the logo animation,
if it was to be re-done I would make it way cleaner but
as-is there is only one 3D element to display in the whole
launcher so...
*/

void main() {
	vec3 p = i_position;
	mat2 rotation = rot2D(u_time);
	p.x *= -1;
	p.xz = rotation*p.xz;
	p.y += sin(u_time);
	p.xz *= 9/16.;
	p.y += u_y;
	
	vec4 screen = vec4(p*.2, 1);
	gl_Position = screen;
	v_uv = i_uv;
	v_normal = i_normal;
	v_normal.x *= -1;
	v_normal.xz = rotation*v_normal.xz;
	v_objectid = uint(i_objectid);
	v_worldpos = screen.xyz;
}
