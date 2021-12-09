
number = 500;
no_of_discards = 0;

discards = zeros(2,number);
non_discs = zeros(2,number);
no_of_discs = 0;

for i = 1 : number
    d = dists(i,1);
    discard = false;
    
    if d > m + threshold || d < m - threshold
        discard = true;
    end
    
    theta = rand() * (pi() / 2);
    x_offset = d * cos(theta);
    y_offset = d * sin(theta);
    
    if discard
        no_of_discards = no_of_discards + 1;
        discards(1,no_of_discards) = x_offset;
        discards(2,no_of_discards) = y_offset;
    else
        non_discs(1,i - no_of_discards) = x_offset;
        non_discs(2,i - no_of_discards) = y_offset;
    end
end


h = figure;
hold on;
p1 = scatter(non_discs(1,1:number - no_of_discards),non_discs(2,1:number - no_of_discards),50,[0,0,0]);
p2 = scatter(discards(1,1:no_of_discards),discards(2,1:no_of_discards),50,[0,0,0],'filled');
%scatter(coords(1,:),coords(2,:));

theta = linspace(0,pi()/2,1000);
circ = linspace(m,m,length(theta));
inner = linspace(m - threshold ,m - threshold ,length(theta));
outer = linspace(m + threshold ,m + threshold ,length(theta));

title(strcat('Pivot Exclusions, ', num2str(currentDim), ' dimensions'));
xlabel('x');
ylabel('y')
p2_label = strcat('exclusive queries, n = ',num2str(no_of_discards));
legend([p1,p2],'non-exclusive queries',p2_label)


% 
% figure;
% hold on;
% scatter(coords(1,:),coords(2,:));
% 
theta = linspace(0,pi()/2,1000);
circ = linspace(m,m,length(theta));
% inner = linspace(m - threshold ,m - threshold ,length(theta));
% outer = linspace(m + threshold ,m + threshold ,length(theta));
polar(theta,circ,'black');
saveas(h,strcat('pivot_plot_non_hilbert_',num2str(currentDim),'.png'));
% polar(theta,inner);
% polar(theta,outer);
hold off;