clear all
P1=[-1,0,0];
P2=[1,0,0];
az=21;
el=26;
figure(20); clf;
t=0.5;
set(gcf, 'Renderer', 'OpenGL')
[x,y,z] = ndgrid(linspace(-3,5),linspace(-5,5),linspace(-10,10));
f=sqrt((x + 1).^2 + (y).^2+ (z).^2) - sqrt((x - 1).^2 + (y).^2 + (z).^2);
[faces,verts,colors] =isosurface(x,y,z,f,2*t,x);
patch('Vertices', verts, 'Faces', faces, ... 
    'FaceVertexCData', colors, ... 
    'FaceColor','interp', ... 
    'edgecolor', 'interp','FaceAlpha',0.7, 'EdgeAlpha',0);
view(az,el);
% light('Position',[20 -25 -15],'Style','local')
lighting GOURAUD
axis([-1.5 4 -4 4 -10 10])
axis square
axis vis3d
g=2*x;
%  isosurface(x,y,z,g,2*t)
%  view(3);
[x,y,z] = ndgrid(linspace(-3.5,3.5),linspace(-3.5,3.5),linspace(-10,10));
[faces2,verts2,colors2] =isosurface(x,y,z,g,2*t,x);
patch('Vertices', verts2, 'Faces', faces2, ... 
    'FaceVertexCData', colors2, ... 
    'FaceColor','interp', ... 
    'edgecolor', 'interp','FaceAlpha',0.5, 'EdgeAlpha',0);
view(az,el);
camlight(az+10,el+5)
lighting GOURAUD
 colormap gray
 hold on
scatter3(1,0,0,200,'r.')
scatter3(-1,0,0,200,'r.')
 %grid on
